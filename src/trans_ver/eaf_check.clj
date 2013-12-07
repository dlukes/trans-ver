(ns trans-ver.eaf-check
  (:require [trans-ver.valtests :as vt]
            [trans-ver.formatting :as frmt]
            [clojure.xml :as xml]
            ;; [clojure.data.xml :as xml]
            ;; prxml
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [clojure.string :as str]))


;; this would work to convert between clojure.xml and clojure.data.xml
;; representations of xml structure (with perhaps a few slight modifications if
;; postwalk could handle records...
;; (defn conv [x]
;;   (if (instance? clojure.lang.PersistentStructMap x)
;;     (clojure.data.xml/map->Element (apply array-map (apply concat (seq x))))
;;     x))
    ;; (-> x
    ;;     ;; bc thread macro inserts previous form as 2nd element in next form
    ;;     (#(if (= (:attrs %) nil)
    ;;         (assoc % :attrs {}) %))
    ;;     (#(if (= (:content %) nil)
    ;;       (assoc % :content '())
    ;;       (assoc % :content (lazy-seq (:content %))))))
    ;; x))

;; (defn parse [eaf-file]
;;   "Just a wrapper for the function to be accessible from this namespace (for
;;   the GUI)."
;;   (xml/parse-str (slurp eaf-file)))

(defn parse [eaf-file]
  "Just a wrapper for the function to be accessible from this namespace (for
  the GUI)."
  (xml/parse eaf-file))

(defn ort [eaf]
  (filter
   #(= (:LINGUISTIC_TYPE_REF (:attrs %)) "ortografický")
   (:content eaf)))

(defn fon [eaf]
  (filter
   #(= (:LINGUISTIC_TYPE_REF (:attrs %)) "fonetický")
   (:content eaf)))

(defn times [eaf]
  (first (filter
          #(= (:tag %) :TIME_ORDER)
          (:content eaf))))

;;;; SEG LENGTHS

(defn seg-length-in-annot [annot]
  ;; take apart annotation
  (let [[align-annot] (:content annot),
        {id :ANNOTATION_ID ts1 :TIME_SLOT_REF1 ts2 :TIME_SLOT_REF2} (:attrs
  align-annot),
        [annot-val] (:content align-annot),
        [cont] (:content annot-val)
        length (vt/ort-length cont)]
    ;; test its length
    (if (> length 25)
      {:id id :ts1 ts1 :ts2 ts2 :cont cont :length length})))

(defn seg-lengths-in-ort [eaf-ort]
  "Return ort segments that are more than 25 tokens long."
  (mapcat seq
   (for [tier eaf-ort
         :let [check
               (->> tier :content
                    (map seg-length-in-annot)
                    (remove nil?))]]
    check)))

(def annot-template
  {:tag :ANNOTATION,
   :content
   [{:tag :ALIGNABLE_ANNOTATION,
     :attrs {}
     :content
     [{:tag :ANNOTATION_VALUE,
       :content
       ["nil"]}]}]})

(def tier-template
  {:tag :TIER,
   :attrs
   {:ANNOTATOR "TransVer",
    :DEFAULT_LOCALE "cs",
    :LINGUISTIC_TYPE_REF "meta",
    :PARTICIPANT "všichni",
    :TIER_ID "KONTROLA DÉLKY SEGMENTŮ"},
   :content []})

(defn summarize-seg-length-errors [eaf eaf-ort times]
  "Return hash-map representation of .eaf file with seg length error tier."
  (let [eaf-atom (atom eaf)
        ;; we need to know at which index we'll be putting the new tier in the
        ;; :content of the eaf
        last-idx (count (:content eaf))]
    (swap! eaf-atom update-in [:content] #(conj % tier-template))
    (doseq [error (seg-lengths-in-ort eaf-ort)]
      (let [{:keys [id ts1 ts2 length]} error,
            err-id (str "err-" id),
            err-ts1 (str "err-" ts1),
            err-ts2 (str "err-" ts2),
            error-annot (assoc-in annot-template [:content 0 :attrs]
                                  {:ANNOTATION_ID err-id,
                                   :TIME_SLOT_REF1 err-ts1,
                                   :TIME_SLOT_REF2 err-ts2})]
        (swap! eaf-atom update-in [:content last-idx :content]
               #(conj % error-annot))))
    (frmt/xmlrepr->str @eaf-atom)))

;; (defn list->vec [elem]
;;   (if (seq? elem)
;;     (vec elem)
;;     elem))

;; (defn summarize-seg-length-errors [eaf eaf-ort times]
;;   "Return hash-map representation of .eaf file with seg length error tier."
;;   (let [eaf-atom (atom (walk/postwalk list->vec eaf))
;;         ;; we need to know at which index we'll be putting the new tier in the
;;         ;; :content of the eaf
;;         last-idx (count (:content eaf))]
;;     (swap! eaf-atom update-in [:content] #(conj % tier-template))
;;     (doseq [error (seg-lengths-in-ort eaf-ort)]
;;       (let [{:keys [id ts1 ts2 length]} error,
;;             err-id (str "err-" id),
;;             err-ts1 (str "err-" ts1),
;;             err-ts2 (str "err-" ts2),
;;             error-annot (assoc-in annot-template [:content 0 :attrs]
;;                                   {:ANNOTATION_ID err-id,
;;                                    :TIME_SLOT_REF1 err-ts1,
;;                                    :TIME_SLOT_REF2 err-ts2})]
;;         (swap! eaf-atom update-in [:content last-idx :content]
;;                #(conj % error-annot))))
;;     @eaf-atom))

;;;; ALIGNMENT

(defn alignment-of-annot [ort-annot fon-annot]
  ;; take apart annotations
  (let [;; the ort annotation
        [ort-align-annot] (:content ort-annot),
        {ort-id :ANNOTATION_ID ts1 :TIME_SLOT_REF1 ts2 :TIME_SLOT_REF2} (:attrs
  ort-align-annot),
        [ort-annot-val] (:content ort-align-annot),
        [ort-cont] (:content ort-annot-val)
        ;; the fon annotation
        [fon-ref-annot] (:content fon-annot),
        {fon-id :ANNOTATION_ID ref :ANNOTATION_REF} (:attrs fon-ref-annot),
        [fon-annot-val] (:content fon-ref-annot),
        [fon-cont] (:content fon-annot-val)
        ;; count alignment tokens (- 1, to be precise)
        ort-tokens (vt/alignment-tokens ort-cont)
        fon-tokens (vt/alignment-tokens fon-cont)]
    (if (not= (count ort-tokens) (count fon-tokens))
      {:ort
       {:cont ort-tokens}
       :fon
       {:cont fon-tokens}})))

(defn alignment-of-ort-and-fon [eaf-ort eaf-fon]
  "Return ort and fon segment pairs which are not aligned."
;  (concat
          (for [ort-tier eaf-ort,
                fon-tier eaf-fon,
                :when (= (-> fon-tier :attrs :PARTICIPANT)
                         (-> ort-tier :attrs :PARTICIPANT)),
                :let [tier-id (str
                               (-> fon-tier :attrs :TIER_ID)
                               " & "
                               (-> ort-tier :attrs :TIER_ID))]]
            {:tier-id tier-id
             :align
             (map alignment-of-annot
                  (:content ort-tier)
                  (:content fon-tier))}))

;             (remove nil?)))))

(defn summarize-alignment-errors [eaf-ort eaf-fon]
  "Return a string which describes alignment errors in eaf-ort vs eaf-fon."
  (apply str
   (flatten
    (for [tier (alignment-of-ort-and-fon eaf-ort eaf-fon)]
      (concat ["\n" (repeat 79 "#")
               "\nVrstva: " (:tier-id tier) "\n"]
              (for [[num al] (map-indexed vector (:align tier))
                    :when al]
                (str "\nSegment č. " (inc num) " v této vrstvě.\n"
                 (frmt/format-alignment-table al))))))))
