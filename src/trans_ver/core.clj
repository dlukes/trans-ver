(ns trans-ver.core
  (:gen-class)
  (require [trans-ver.gui :as gui]
           [trans-ver.cli :as cli]))

(def help
"TransVer checks the validity of transcriptions for ORTOFON corpus.

Usage: transver [--cli] [--help]

Notes:

transver: substitute the way you invoke the program on the command line
  (generally something along the lines of `java -jar <trans-ver-uberjar>`)

--cli: command line batch mode; only checks alignment and dumps to STDOUT;
  currently undocumented

--help: print this message

By default (without any switches), TransVer starts in a GUI mode which is
documented at <https://github.com/dafydd-lukes/trans-ver>.
")

(defn -main [& args]
  (let [switch (first args)]
    (cond
      (= switch "--help") (println help)
      (= switch "--cli") (doseq [eaf-path (rest args)] (cli/run-checks eaf-path))
      :else (gui/initialize-gui))))
