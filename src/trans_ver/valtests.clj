(ns trans-ver.valtests
  (require [clojure.string :as str]))

(defn foo [] (println "foo"))

;;;; SEGMENT LENGTHS

(defn tokenize-ort [seg]
  (->>
   (-> seg
       (str/replace #"\s+#" "")
       (str/replace #"<[\p{Lu}_]+" ""))
   (re-seq #"\p{L}+|&+|\(\d+\)")))

(defn ort-length [seg]
  (count (tokenize-ort seg)))

;;;; ORT & FON ALIGNMENT

(defn alignment-tokens [seg]
  ;; bacha! mezi ort a fon se mažou "?" a "#"
  ;; taky pozor na whitespace před a za posledními písmeny (na případných
  ;; neshodách v tomhle zhavarovat nechceme)
  (let [cleaned-seg (-> seg
                        ;; vsunout placeholdery za slova bez fon realizace
                        (str/replace #"\|([^\p{Ll}_])" "|SLOVOBEZFONREALIZACE$1")
                        (str/replace #"([^\p{Ll}_])\|" "$1SLOVOBEZFONREALIZACE|")
                        (str/replace #"\n" " ")
                        (str/replace #"^\s+" "")
                        (str/replace #"\s+$" "")
                        ;; scuknout otazníky ...
                        (str/replace #"\s+\?" "?")
                        ;; ... označit hranice tokenů...
                        (str/replace #"(\s+|\|)" "$1HRANICETOKENU")
                        ;; ... a zase rozcuknout otazníky
                        (str/replace #"\?" " ?")
                        )]
      (str/split cleaned-seg #"HRANICETOKENU")))

      ;; (re-seq #"\S+\s+(?:\?\S*)?" cleaned-seg)
      ;; (re-seq #"[^\s\|]+(?:\s+|\|)" cleaned-seg))))
