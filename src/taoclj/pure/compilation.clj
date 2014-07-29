(ns taoclj.pure.compilation
  (:require [taoclj.pure.util :refer [in?]]
            [taoclj.pure.parsers :as parsers]
            [taoclj.pure.compilation.validation :as v]))



(defn condition-error [path type condition]
  (throw (Exception. (str path " Invalid condition " type " / " condition))))


(defn compile-parser [path rule]
  (case (first rule)
    :string    parsers/parse-string
    :int       parsers/parse-int
    :datetime  parsers/parse-datetime
    :email     parsers/parse-email
    (throw (Exception. (str "Unknown type in the rule for " path)))))


(defn read-parser-opts [rule]
  (case (first rule)

    :datetime
    (let [opts (second rule)]
      (if (string? opts) {:default opts} opts))

    nil ))


(defn extract-raw-conditions [rule]
  (let [[min nx] (case (first rule) :datetime [3 nnext] [2 next])]
    (if-not (> (count rule) min) []
      (-> rule nx drop-last))))



(defn compile-string-length-condition [min max]
  (fn [_ value _]
    (if (nil? value) true
      (let [len (count value)
            short (and (not (nil? min)) (< len min))
            long (and (not (nil? max)) (> len max))]
        (not (or short long))))))


(defn compile-cross-key-condition [condition]
  (fn [values value _]
    (let [f    (second condition)
            ks   (drop 2 condition)
            xval (get-in values ks) ]
        (f value xval))))


(defn compile-int-range-condition [min max]
  (fn [_ value _]
    (if (nil? value) true
    (let [under (and (not (nil? min)) (or (nil? value) (< value min)))
          over (and (not (nil? max)) (or (nil? value) (> value max)))]
      (not (or under over))))))


(defn compile-regex-condition [rx]
  (fn [_ value _]
    (cond (nil? value)  true
          :else         (not (nil? (re-find rx value))))))


(defn compile-condition [path datatype condition]

  (cond (= condition :required)
        (fn [_ value _] (not (nil? value)))


        (vector? condition)
        (let [name (first condition)]
          (case [datatype name]

            [:string :length]
            (compile-string-length-condition (second condition) (nth condition 2))

            [:int :range]
            (compile-int-range-condition (second condition) (nth condition 2))


            [:string :*]
            (compile-cross-key-condition condition)

            [:int :*]
            (compile-cross-key-condition condition)


            :else
            (condition-error datatype condition)))


        ; allow plain regex conditions
        (= (str (class condition)) "class java.util.regex.Pattern")
        (compile-regex-condition condition)


        ; custom conditions we just use the function directly
        (fn? condition)
        condition


        :else
        (condition-error path datatype condition)

        ))







(defn compile-conditions
  "Convert raw conditions into compiled condition functions"
  [path datatype raw-conditions]
  (for [condition raw-conditions]
    (compile-condition path datatype condition)))



(defn compile-rule
  "Reads and compiles the simplified rule syntax."
  [path rule]

  (let [length (count rule)
        datatype   (first rule)
        errors (last rule)
        raw-conditions (extract-raw-conditions rule)]

    (v/validate-rule-size       path rule)
    (v/validate-rule-types      path rule)
    (v/validate-rule-errors     path rule)
    (v/validate-rule-conditions path datatype raw-conditions)

    [path {:parser      (compile-parser path rule)
           :parser-opts (read-parser-opts rule)
           :conditions  (compile-conditions path datatype raw-conditions)
           :errors      (if (string? errors) {:default errors} errors) }]

    ))


(defn get-paths
  "Extracts the key sequences for all keys, even nested keys from a map.

  (get-paths {:name 'name
               :address {:street 'street}})

  => ([:name] [:address :street])
  "
  [m]
  (cond (not (map? m))  '(())
        (empty? m)      '(())
        :else
        (for [[k v] m
              subkey (get-paths v)]
            (vec (cons k subkey)))))

;; (get-paths {:name 'name
;;             :address {:street 'street}})



(defn compile-model
  "Compiles/prepares a pure model for use by check"
  [model]
  (into {}
        (map (fn [path]
         (compile-rule path (get-in model path)))

         (get-paths model) )))


;; (compile-model

;;   {:password [:string "pe*"]
;;    :confirm  [:string [:* = :password] "ce*"]}

;;   )



; (compile-model {:name [:string :required "e*"]})




























