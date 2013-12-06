(defproject trans-ver "0.1.0-SNAPSHOT"
  :description "Transcription Verifier for spoken data collection at CNC [korpus.cz]"
  :url "http://example.com/FIXME"
  :license {:name "GPLv3"
            :url "http://www.gnu.org/licenses/gpl.txt"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [seesaw "1.4.4"]]
  :main ^:skip-aot trans-ver.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
