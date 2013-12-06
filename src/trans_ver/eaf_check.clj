(ns trans-ver.eaf-check
  (:require [trans-ver.valtests :as vt]
            [trans-ver.formatting :as frmt]
            [clojure.xml :as xml]))

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
