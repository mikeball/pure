(ns taoclj.pure.rules
  (:require [taoclj.pure.util :as util]
            [taoclj.pure.compilation :refer [read-rule]]
            [taoclj.pure.parsing :as parsing]))



(defmulti check-condition
  "Returns false if built in condition check fails,
   error message string from custom fuction checks.

  (check-condition :string :required :default nil)
  => false

  (check-condition :string :required :default \"z\")
  => true

  (check-condition :string [:length 2 5] :default \"x\")
  => false

  "

  (fn [type condition culture value]

    (cond (= condition :required) :required
          ; (keyword? condition)  [type condition]
          (vector? condition)     [type (first condition)]
          (fn? condition)         :custom

          :default (throw (Exception. (str "Invalid condition - " type " / " condition)))

          )))

(defmethod check-condition :required [_ _ _ value]
  (not (nil? value)))

(defmethod check-condition :custom [_ condition culture value]
  (condition culture value))

(defmethod check-condition [:string :length] [_ condition _ value]
  (if (nil? value) true
    (let [len (count value)
        min (second condition)
        max (nth condition 2)
        short (and (not (nil? min)) (< len min))
        long (and (not (nil? max)) (> len max))]

      (not (or short long)))))

(defmethod check-condition [:int :range] [_ condition _ value]
  (if (nil? value) true
    (let [minumum (second condition)
          maximum (nth condition 2)
          under (and (not (nil? minumum)) (or (nil? value) (< value minumum)))
          over (and (not (nil? maximum)) (or (nil? value) (> value maximum)))]
      (not (or under over)))))


; (check-condition :int [:range 2 5] :default nil)




;; ; implement datetime like this
;; (check-condition :string [:datetime "mm/dd/yyyy"] :default "")
;; (check-condition :string
;;                  [:datetime {:default "mm/dd/yyyy"
;;                              :de-de "yyyy/mm/dd"}]
;;                  :default "")


(defn check-conditions
  "Checks a sequence of conditions against a raw value.

  (check-conditions :string [:required [:length 2 5]] :default \"x\")
  => false
  "
  [type conditions culture value]
  (first (filter #(or (false? %) (string? %))
                  (for [condition conditions]
                    (check-condition type condition culture value)))))



(defn rule-fn
  "Builds and returns a function that parses/checks a rule and returns a summary.

  (let [myfn (rule-fn :id [:string :required \"e*\"])]
    [(myfn \"\")
     (myfn \"x\")])

  => [{:ok false :msg \"e*\" :val nil}
      {:ok true, :val \"x\"}]
  "
  [field rule]

  (let [rr (read-rule field rule)
        type       (:type rr)
        msgs       (:messages rr)
        required   (:required rr)
        conditions (:conditions rr)]

    ; (println "*** required: " required)
    (fn [raw & culture-code] ; generate function that parses & applies all checks

      ; parse the raw data
      (let [cc (first culture-code)
            culture (if (and cc (msgs cc)) cc :default)
            parsed (parsing/parse type raw)
            value (:val parsed)]

        (cond  ; parsing failure return error
               (not (:ok parsed))     (assoc parsed :msg (culture msgs))

               ; required and nil => error
               (and required
                    (nil? value))     (assoc parsed :ok false :msg (culture msgs))

               :else  ; check conditions in order
               (let [failure (check-conditions type conditions cc value)]

                 (if-not (nil? failure)
                   (assoc parsed :ok false :msg (if (string? failure) failure (culture msgs)))

                   parsed)))))))



; ((rule-fn :id [:int [:range 2 4] "e*"])
;  "2")

