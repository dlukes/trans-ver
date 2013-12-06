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
                        (str/replace #"\n" " ")
                        (str/replace #"^\s+" "")
                        (str/replace #"\s+$" "")
                        ;; scuknout otazníky ...
                        (str/replace #"\s+\?" "?")
                        ;; ... označit hranice tokenů...
                        (str/replace #"(\s+|\|)" "$1^")
                        ;; ... a zase rozcuknout otazníky
                        (str/replace #"\?" " ?")
                        )]
      (str/split cleaned-seg #"\^")))

      ;; (re-seq #"\S+\s+(?:\?\S*)?" cleaned-seg)
      ;; (re-seq #"[^\s\|]+(?:\s+|\|)" cleaned-seg))))
