(ns trans-ver.eaf-check
  (:require [trans-ver.valtests :as vt]
            [trans-ver.formatting :as frmt]
            [clojure.xml :as xml]
            [clojure.string :as str]))

;; this would work to convert between clojure.xml and clojure.data.xml
;; representations of xml structure (with perhaps a few slight modifications) if
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
  "Return idx and id->val map of TIME_ORDER element."
  (let [idx (inc (last
                  (for [[idx tier] (map-indexed vector (:content eaf))
                        :while (not (= (:tag tier) :TIME_ORDER))]
                    idx)))
        id->val (reduce #(assoc %1 (get-in %2 [:attrs :TIME_SLOT_ID])
                                (get-in %2 [:attrs :TIME_VALUE]))
                        {} (get-in eaf [:content idx :content]))]
    [idx id->val]))


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
    :LINGUISTIC_TYPE_REF "ortografický",
    :PARTICIPANT "všichni",
    :TIER_ID "KONTROLA DÉLKY SEGMENTŮ"},
   :content []})

(def timeslot-template
  {:tag :TIME_SLOT,
   :attrs {},
   :content nil})

(defn kontrola-tier? [tier]
    (or
     (= (get-in tier [:attrs :TIER_ID])
        (get-in tier-template [:attrs :TIER_ID]))
     (= (get-in tier [:attrs :ANNOTATOR])
        (get-in tier-template [:attrs :ANNOTATOR]))))

(defn remove-kontrola-tier [eaf]
  "Remove previous KONTROLA tier if it exists."
  (update-in eaf [:content] #(vec (remove kontrola-tier? %))))

(defn summarize-seg-length-errors [eaf eaf-ort times]
  "Return hash-map representation of .eaf file with seg length error tier."
  (let [eaf-atom (atom (remove-kontrola-tier eaf))
        ;; we need to know at which index we'll be putting the new tier in the
        ;; :content of the eaf
        last-idx (count (:content @eaf-atom))
        ;; and the idx and id->val map of the TIME_ORDER tier
        [times-idx times-id->val] times]
    (swap! eaf-atom update-in [:content] #(conj % tier-template))
    (doseq [error (seg-lengths-in-ort eaf-ort)]
      (let [{:keys [id ts1 ts2 length]} error,
            err-id (str "err-" id),
            err-ts1 (str "err-" ts1),
            err-ts2 (str "err-" ts2),
            ts1-val (times-id->val ts1),
            ts2-val (times-id->val ts2),
            error-annot (-> annot-template
                            (assoc-in [:content 0 :attrs]
                                      {:ANNOTATION_ID err-id,
                                       :TIME_SLOT_REF1 err-ts1,
                                       :TIME_SLOT_REF2 err-ts2})
                            (assoc-in [:content 0 :content 0 :content 0]
                                      (str "PŘÍLIŠ DLOUHÝ SEGMENT: " length))),
            ts1-annot (assoc timeslot-template :attrs
                             {:TIME_SLOT_ID err-ts1, :TIME_VALUE ts1-val}),
            ts2-annot (assoc timeslot-template :attrs
                             {:TIME_SLOT_ID err-ts2, :TIME_VALUE ts2-val})]
        ;; add the new error annotation
        (swap! eaf-atom update-in [:content last-idx :content]
               #(conj % error-annot))
        ;; add its time indices to the TIME_ORDER element
        (swap! eaf-atom update-in [:content times-idx :content]
               #(conj % ts1-annot ts2-annot))))
    (frmt/xmlrepr->str @eaf-atom)))

;;;; ALIGNMENT

(defn matches? [regex string]
  ((complement nil?) (re-find regex string)))

;; - může hrát i roli spojovníku, takže v těsném sousedství s alfabetickým
;; znakem je specifické pro ort; zahodíme taky čísla, nemusí si odpovídat (ČT2
;; vs. čé=te=dva); a nakonec budeme ignorovat i kulaté závorky, zas tak moc na
;; nich nezáleží
(def symbols-that-need-not-match-between-tiers
  #"-\p{L}|\p{L}-|[$\{\}_\|=\*#\?\s\p{L}\d\(\)]")

(defn valid-counterparts? [[str1 str2]]
  "True if:

  - both strings in argument vector contain alphabetic characters
    (including _ and |, which are parts of words on fon) or digits
    (= unintelligible words), or none of them does

  AND

  - after stripping all alphabetic characters + whitespace + symbols that are
    not required to be parallel, or are only used on the ort or fon layer ({,
    }, _, |, =, *, #, ?), both strings are the same (i.e., all shared auxiliary
    characters must be the same)

  Remember that 'SLOVOBEZFONREALIZACE' has been substituted for words
  empty on fon, so they are treated as regular words."
  (let [match1 (matches? #"[\p{L}_\|\d]" str1),
        match2 (matches? #"[\p{L}_\|\d]" str2),
        meta1 (str/replace str1 symbols-that-need-not-match-between-tiers "")
        meta2 (str/replace str2 symbols-that-need-not-match-between-tiers "")]
    (and
     (or
      (and match1 match2)
      (and (not match1) (not match2)))
     (= meta1 meta2))))

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
    ;; example ort-tokens and fon-tokens:
    ;; ["+ " "to " "já " "nevím " ".."]
    ;; ["to " "|" "já " "nevím " ".."]
    (if (or
         (not= (count ort-tokens) (count fon-tokens))
         (not (every? valid-counterparts? (map vector ort-tokens
                                               fon-tokens)))
         ;; _ is an indicator that a word boundary should follow; if there's
         ;; none (i.e. if _ is followed by a letter) → transcription error
         (some (partial matches? #"_\p{Ll}") fon-tokens))
      ;; (do (pprint ort-tokens) (pprint fon-tokens)
      {:ort
       {:cont ort-tokens}
       :fon
       {:cont fon-tokens}
       :TIME_SLOT_REF1 ts1})))

(defn alignment-of-ort-and-fon [eaf-ort eaf-fon]
  "Return ort and fon segment pairs which are not aligned."
;  (concat
          (for [ort-tier eaf-ort,
                fon-tier eaf-fon,
                :when (= (-> fon-tier :attrs :PARENT_REF)
                         (-> ort-tier :attrs :TIER_ID)),
                :let [tier-id (str
                               (-> fon-tier :attrs :TIER_ID)
                               " & "
                               (-> ort-tier :attrs :TIER_ID))]]
            {:tier-id tier-id
             :align
             (map alignment-of-annot
                  (:content ort-tier)
                  (:content fon-tier))}))

(defn summarize-alignment-errors [eaf-ort eaf-fon times-id->val]
  "Return a string which describes alignment errors in eaf-ort vs eaf-fon."
  (apply str
   (flatten
    (for [tier (alignment-of-ort-and-fon eaf-ort eaf-fon)]
      (concat ["\n" (repeat 30 "-")
               "\nVrstva: " (:tier-id tier) "\n"]
              (for [[num al] (map-indexed vector (:align tier))
                    :when al]
                (str "\nSegment č. " (inc num) " v této vrstvě; čas: "
                     (frmt/millis->readable
                      (times-id->val (:TIME_SLOT_REF1 al))) ".\n"
                     (frmt/format-alignment-table al))))))))
