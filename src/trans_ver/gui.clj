(ns trans-ver.gui
  (require [trans-ver.eaf-check :as eaf]
           [clojure.java.io :as io])
  (use seesaw.core
       seesaw.dev
       seesaw.font
       seesaw.mig
       seesaw.chooser))

(native!)

(def main-frame (frame :title "ÚČNK TransVer"
                       :size [640 :by 480]))

(defn display [content]
  (config! main-frame :content content)
  content)

(def feedback-area (text :editable? false
                  :multi-line? true
                  :font "MONOSPACED"))

;(text! feedback-area (eaf/summarize-alignment-errors eaf/eaf-ort eaf/eaf-fon))

;;;; FILE CHOOSING

(defn choose-eaf-file [e]
  (text! e (str (choose-file))))

(def choose-eaf-file-field
  (text :text "Vybrat soubor..."
        :listen [:mouse-clicked (fn [e] (choose-eaf-file e))]))

;;;; RUNNING CHECKS

(defn run-checks [e]
  (let [file (text choose-eaf-file-field)]
    (if (.exists (io/as-file file))
      (let [eaf (eaf/parse file)
            ort (eaf/ort eaf)
            fon (eaf/fon eaf)]
        (text! feedback-area
               (eaf/summarize-alignment-errors ort fon)))
      (alert "Zadaná cesta k souboru není platná."))))

(def run-checks-button
  (button :text "Spustit"
          :listen [:action run-checks]))

(display (border-panel
          :north (horizontal-panel :items [choose-eaf-file-field run-checks-button])
          :center (scrollable feedback-area)
          :vgap 5 :hgap 5 :border 5))

(defn initialize-gui []
  (invoke-later
   (show! main-frame)))
