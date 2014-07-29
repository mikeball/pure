(ns taoclj.pure
  (:require [clojure.string :only [blank? trim] :as str]
            [taoclj.pure.compilation :as compilation]))



(defn parse-raw-value
  "Parses a single raw value.

  (parse-raw-value compiled-model [:age] {:age \"21\"})
  => [[:age] {:ok true :val 21}]

  "
  [model path raw culture]
  (let [parser      (get-in model [path :parser])
        parser-ops  (get-in model [path :parser-opts])]
    [path (parser (get-in raw path) (culture parser-ops))]))

;; (parse-raw-value
;;    {[:birth] {:parser parsers/parse-datetime
;;               :parser-opts {:default "MM/dd/yyyy"}}}
;;                  [:birth]
;;                  {:birth "6/1/2014"}
;;                  :default)


(defn parse-raw
  "Parses all raw values in a raw data map.

  (parse-raw '([:name])
             {[:name] {:parser parsing/parse-string}}
             {:name \" bob \"})
  => {[:name] {:ok true :val \"bob\"}}
  "
  [parse-keys model raw culture]
  (into {} (map (fn [path] (parse-raw-value model path raw culture))
                parse-keys)))

;; (require '[taoclj.pure.parsers :as parsers])

;; (parse-raw '([:birth])
;;             {[:birth] {:parser parsers/parse-datetime
;;                        :parser-opts {:default "MM/dd/yyyy"}}}
;;             {:birth "6/1/2014"}
;;             :default
;;            )



(defn extract-values
  "Pulls parsed values and places them into a origial map structure.

  (extract-values {[:name] {:ok true :val \"bob\"}})
  => {:name \"bob\"}
  "
  [parse-results]
  (reduce (fn [parsed path]
            (assoc-in parsed path (get-in parse-results [path :val])))
          {}
          (keys parse-results)))



(defn check-field-conditions
  [field-path culture conditions value values]

  (first (filter #(not (nil? %))
                 (map (fn [condition]
                        (let [result (condition values value culture)]
                          (cond (true? result)     nil
                                (string? result)   {:field field-path :msg result}
                                :else              field-path

                                )))
                      conditions )
                 )) )


;; (check-field-conditions [:name]
;;                         :en-us
;;                         [required-ok?
;;                          ; string-length-ok?

;; ;;                          (fn [value culture]
;; ;;                            (if-not (= value "bob") true
;; ;;                              (str "Sorry taken " culture)))

;;                          ]

;;                         nil)








(defn get-errors
  "Builds final error message map from list of failures.

  (get-errors
    {[:name] {:errors {:default \"Name is required\"}}}
    :default
    '([:name] {:field [:username] :msg \"Username taken\"}))

  => {:name \"Name is required\" :username \"Username taken\"}
  "
  [model culture failures]

  (reduce (fn [errors failure]

            (let [custom (map? failure)
                  path   (if custom (:field failure) failure)
                  msg    (if custom (:msg failure)
                           (if-let [msg (get-in model [path :errors culture])]
                             msg
                             (get-in model [path :errors :default])))]

              (assoc-in errors path msg)

              ))
          {}

          failures
    ) )


;; (get-errors {[:name] {:error {:default "name is required"}}}
;;             :default
;;             '([:name] {:field [:username] :msg "That username is taken"}))



(defn check

  ([model raw] (check model raw :default))

  ([model raw culture]

  (let [model-keys (keys model)

        parse-results  (parse-raw model-keys model raw culture)

        values  (extract-values parse-results)

        parsed-ok     (filter #(true? (get-in parse-results [% :ok]))
                              model-keys )

        parse-failures   (filter #(false? (get-in parse-results [% :ok]))
                                 model-keys )

        condition-failures (filter #(not (nil? %))
                                   (for [path parsed-ok]
                                     (check-field-conditions path
                                                             culture
                                                             (get-in model [path :conditions])
                                                             (get-in parse-results [path :val])
                                                             values)
                                     ))

        all-failures (concat parse-failures condition-failures)

        result        {:raw raw :values values}
        ]


    (if (empty? all-failures) result

      (assoc result
        :errors (get-errors model culture all-failures))
      ))

   ))


;; (check
;;  (compilation/compile-model  {:password [:string "pe*"]
;;                               :confirm  [:string [:* = :password] "ce*"]} )
;;        {:password "a"
;;         :confirm "a"}
;;        )





(defmacro defm
  "A simple way to define a pure model"
  [name & model]
  `(def ~name (compile-model ~@model)))





;; (check (compilation/get-compiled {:name [:string "e*"]})
;;        {:name " bob "}
;;        )


;; (check {[:name] {:parser parsing/parse-string :error {:en-us "Error"}}}
;;        :en-us
;;        {:name "2"})



;; (check compiled-model
;;        :default
;;        {:name "bob"
;;          :age "21"
;;          :address {:street " 123 Oak "}}
;;         )






























