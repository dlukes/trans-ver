(ns trans-ver.core
  (:gen-class)
  (require [trans-ver.gui :as gui]))

(defn -main [& args]
  (gui/initialize-gui))
