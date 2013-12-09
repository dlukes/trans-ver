(ns trans-ver.gui
  (require [trans-ver.eaf-check :as eaf]
           [trans-ver.formatting :as frmt]
           [clojure.java.io :as io]
           [clojure.string :as str])
  (use seesaw.core
       seesaw.dev
       seesaw.font
       seesaw.mig
       seesaw.chooser))

(native!)

;;;; USEFUL VARIABLES

(def file-sep (System/getProperty "file.separator"))

;;;; MAIN FRAME

(def main-frame (frame :title "ÚČNK TransVer"
                       :size [640 :by 480]
                       :on-close :exit))

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
   (combobox
    :listen [:action (fn [e]
                       (config! feedback-area
                                :font (str "MONOSPACED-"
                                           (selection font-size-combobox))))]
    :model [10 12 14 16 18 20]))

;; FIXME: somehow, when this is done with a thread macro in the definition
;; above, the file refuses to compile:

;; CompilerException java.lang.IllegalArgumentException: No implementation of
;; method: :get-selection of protocol: #'seesaw.selection/Selection found for
;; class: nil, compiling:(gui.clj:52:4)

(selection! font-size-combobox 12)

;;; task selection

(def select-tasks-checkboxes
  ;; return directly [widget, constraint] pairs for mig-panel
  (map #(vector (checkbox :id %1 :text %2 :tip %3) %4)
       [:align :lengths :fon]
       ["Alignace" "Délky segmentů" "Přepisu fon"]
       ["Zkontrolovat, zda si odpovídají \"slova\" na vrstvách ort a fon."
        "Označit segmenty delší než 25 \"slov\"."
        "Zatím není implementováno :("]
       (repeat "wrap")))

;;;; RUNNING CHECKS

(defn check-seg-lengths [eaf file]
  (let [new-eaf-file (str (.getParent (.getAbsoluteFile file))
                          file-sep
                          (str/replace (.getName file) #"\.eaf" " ")
                          (frmt/windows-filename-compliant-string (new java.util.Date))
                          ".eaf")
        feedback-str (str "Vytvořen soubor \"" new-eaf-file
                      "\" s vrstvou KONTROLA DÉLKY SEGMENTŮ.\n\n")]
    (spit new-eaf-file
          (eaf/summarize-seg-length-errors eaf (eaf/ort eaf) (eaf/times eaf)))
    (str feedback-str (apply str (repeat (- (count feedback-str) 2) "#")) "\n\n")))

(defn run-checks [e]
  (let [file (io/as-file (text choose-eaf-file-field))]
    (if (.exists file)
      (let [eaf (eaf/parse file)
            ort (eaf/ort eaf)
            fon (eaf/fon eaf)]
        (text! feedback-area (str
                              ;; seg lengths checks
                              (if (selection (select main-frame [:#lengths]))
                                (str "KONTROLA DÉLKY SEGMENTŮ\n"
                                     (check-seg-lengths eaf file)))
                              ;; alignment checks
                              (if (selection (select main-frame [:#align]))
                                (str "KONTROLA ALIGNACE ORT A FON\n"
                                     (eaf/summarize-alignment-errors ort fon
                                                                     (second
  (eaf/times eaf)))))))
        (scroll! feedback-area :to :top))
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
   (config! (select main-frame [:#fon]) :enabled? false)
   (selection! (select main-frame [:#align]) true)))

; (initialize-gui)
