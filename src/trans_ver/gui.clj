(ns trans-ver.gui
  (require [trans-ver.eaf-check :as eaf]
           [clojure.java.io :as io])
  (use seesaw.core
       seesaw.dev
       seesaw.font
       seesaw.mig
       seesaw.chooser))

(native!)

;;;; HELPER FUNCTIONS

(defn display [content]
  (config! main-frame :content content)
  content)

;;;; MAIN FRAME

(def main-frame (frame :title "ÚČNK TransVer"
                       :size [640 :by 480]))

;;;; FEEDBACK AREA

(def feedback-area (text :editable? false
                  :multi-line? true
                  :font "MONOSPACED"))

;;;; FILE CHOOSING

(defn choose-eaf-file [e]
  (text! e (str (choose-file))))

(def choose-eaf-file-field
  (text :text "Vybrat soubor..."
        :listen [:mouse-clicked (fn [e] (choose-eaf-file e))]))

;;;; SETTINGS

;;; font size

(def font-size-combobox
  (combobox
                                        ;:maximum-size [100 :by 50]
   :model [10 12 14 16]))
                                        ;                  :preferred-size [10 :by 10])))

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
          ;; :east (vertical-panel :items select-tasks-checkboxes)
          ;; :east (vertical-panel :border ""
          ;;        :items (concat select-tasks-checkboxes
          ;;                                      font-size-combobox))
          ;; :east select-tasks-checkboxes
          ;;               font-size-combobox)
          :east (mig-panel :items
                           `[["Proveď kontrolu:" "wrap"]
                             ~@select-tasks-checkboxes
                             ["Velikost písma:" "wrap"]
                             [~font-size-combobox "wrap"]])
          ;; :east (vertical-panel :items
          ;;                  ["Proveď kontrolu:"
          ;;                   ;[select-tasks-checkboxes ""]
          ;;                   "Velikost písma:"
          ;;                   font-size-combobox])
          :vgap 5 :hgap 5 :border 5))

(defn initialize-gui []
  (invoke-later
   (show! main-frame)
   (config! (select main-frame [:#fon]) :enabled? false)))

(initialize-gui)
