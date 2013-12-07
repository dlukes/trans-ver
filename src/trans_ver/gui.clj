(ns trans-ver.gui
  (require [trans-ver.eaf-check :as eaf]
           [clojure.java.io :as io])
  (use seesaw.core
       seesaw.dev
       seesaw.font
       seesaw.mig
       seesaw.chooser))

(native!)

;;;; MAIN FRAME

(def main-frame (frame :title "ÚČNK TransVer"
                       :size [640 :by 480]))

;;;; HELPER FUNCTIONS

(defn display [content]
  (config! main-frame :content content)
  content)

;;;; FEEDBACK AREA

(def feedback-area (text :editable? false
                  :multi-line? true
                  :font "MONOSPACED-12"))

;;;; FILE CHOOSING

(defn choose-eaf-file [e]
  (text! e (str (choose-file))))

(def choose-eaf-file-field
  (text :text "Vybrat soubor..."
        :listen [:mouse-clicked (fn [e] (choose-eaf-file e))]))

;;;; SETTINGS

;;; font size

;(declare change-font-size)

(def font-size-combobox
  (->
   (combobox
    :listen [:action (fn [e]
                       (config! feedback-area
                                :font (str "MONOSPACED-"
                                           (selection font-size-combobox))))]
    :model [10 12 14 16 18 20])
   (selection! 12)))

;;; task selection

(def select-tasks-checkboxes
  ;; return directly [widget, constraint] pairs for mig-panel
  (map #(vector (checkbox :id %1 :text %2 :tip %3) %4)
       [:align :lengths :fon]
       ["Alignace" "Délky segmentů" "Přepisu fon"]
       ["FIXME" "FIXME" "FIXME"]
       (repeat "wrap")))

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
          :east (mig-panel :items
                           `[["Proveď kontrolu:" "wrap"]
                             ~@select-tasks-checkboxes
                             ["Velikost písma:" "wrap"]
                             [~font-size-combobox "wrap"]])
          :vgap 5 :hgap 5 :border 5))

(defn initialize-gui []
  (invoke-later
   (show! main-frame)
   (config! (select main-frame [:#fon]) :enabled? false)))

(initialize-gui)
