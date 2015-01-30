(ns trans-ver.cli
  (require [trans-ver.eaf-check :as eaf]
           [clojure.java.io :as io]))

;; (defn check-seg-lengths [eaf]
;;   (let [new-eaf-file (str (.getParent (.getAbsoluteFile file))
;;                           file-sep
;;                           (str/replace (.getName file) #"\.eaf" " ")
;;                           (frmt/windows-filename-compliant-string (new java.util.Date))
;;                           ".eaf")
;;         feedback-str (str "Vytvořen soubor \"" new-eaf-file
;;                       "\" s vrstvou KONTROLA DÉLKY SEGMENTŮ.\n\n")]
;;     (spit new-eaf-file
;;           (eaf/summarize-seg-length-errors eaf (eaf/ort eaf) (eaf/times eaf)))
;;     (str feedback-str (apply str (repeat (- (count feedback-str) 2) "#")) "\n\n")))


(defn run-checks [eaf-path]
  (let [file (io/as-file eaf-path)]
    (if (.exists file)
      (let [eaf (eaf/parse file)
            ort (eaf/ort eaf)
            fon (eaf/fon eaf)
            align-summary (str (eaf/summarize-alignment-errors ort fon (second (eaf/times eaf))))]
        ;; seg lengths checks
        ;; (print
        ;;  (str "KONTROLA DÉLKY SEGMENTŮ\n"
              ;; (check-seg-lengths eaf)))
        ;; alignment checks
        (when (.contains align-summary "|")
          (println eaf-path)
          (println align-summary)
          (println)))
      (print (str "Zadaná cesta k souboru není platná: " eaf-path)))))
