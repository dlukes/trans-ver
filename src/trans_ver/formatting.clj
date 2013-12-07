(ns trans-ver.formatting
  (require [clojure.walk :as walk]
           [clojure.string :as str]
           [clojure.xml :as xml]))
;  (require [trans-ver.eaf-check :as eaf]))

;; format alignment table

(defn max-seq [coll]
  "Returns maximum length element in coll."
  (reduce #(if (> (count %1)
                  (count %2))
             %1
             %2)
          coll))

(defn extend-seq [n seq]
  "Adds empty strings to seq until it has n elements overall."
  (concat seq (repeat (- n (count seq)) "")))

(defn same-length-seqs [seq-of-seqs]
  "Extends shorter seqs in seq-of-seqs to be as long as the longest one."
  (let [max (count (max-seq seq-of-seqs))]
    (map (partial extend-seq max) seq-of-seqs)))

(defn format-alignment-table [wrong-alignment]
  (let [[ort fon]
        (same-length-seqs
         [(:cont (:ort wrong-alignment)) (:cont (:fon wrong-alignment))]),
        max-ort (max (count (max-seq ort)) 3), ; 3 is the minimum width because
                                        ; of header (ort/fon)
        max-fon (max (count (max-seq fon)) 3),
        header (format (str "| %" max-ort "s | %" max-fon "s |\n") "ort" "fon"),
        separator (apply str (repeat (dec (count header)) "="))]
    (apply str header separator "\n"
     (map #(format (str "| %" max-ort "s | %" max-fon "s |\n") %1 %2) ort fon))))

;; format output XML (eaf) with KONTROLA tier

(defn apostrophe->tab [elem]
  "If elem is a string, replace former tabs with spaces and apostrophes with
  tabs."
  (if (string? elem)
    (-> elem
        (str/replace #"\t" " ")
        (str/replace #"'" "\t"))
    elem))

(defn xmlrepr->str [eaf]
  (-> eaf
      (#(walk/postwalk apostrophe->tab %))
      (#(with-out-str (xml/emit %)))
      (str/replace #"'" "\"")
      (str/replace #"\t" "'")))
