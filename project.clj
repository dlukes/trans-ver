(defproject trans-ver "0.5.0"
  :description "Transcription Verifier for spoken data collection at CNC [korpus.cz]"
  :url "http://github.com/dafydd-lukes/trans-ver"
  :license {:name "GPLv3"
            :url "http://www.gnu.org/licenses/gpl.txt"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [seesaw "1.4.4"]]
                 ;; [prxml "1.3.1"]
                 ;; [org.clojure/data.xml "0.0.7"]]
  :main ^:skip-aot trans-ver.core
  :target-path "target/%s"
  :javac-target "1.6"
  :profiles {:uberjar {:aot :all}
             ;:uberjar {:aot [trans-ver.core]}
             })
