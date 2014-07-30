(ns taoclj.pure.compilation.validation
  (:require [taoclj.pure.util :refer [in?]]))


(defn error [path message]
  (throw (Exception. (str "Error found in the rule for " path ". " message))))


(defn validate-rule-size [path rule]
  (if-not (sequential? rule) (error path "Rules must be sequential"))
  (let [length (count rule) type (first rule)]
    (cond (= type :datetime) (if (< length 3)
                               (error path "Datetime rules must have at least 3 elements"))
          :default
          (if (< length 2)
            (error path "Rules must have at least 2 elements")))))

(defn validate-rule-types [path rule]
  (if-not (in? [:int :string :email :datetime] (first rule))
    (error path
           "Invalid type in first element of rule. Allowed types are :int, :string and :email.")))

(defn validate-rule-errors [path rule]
  (let [errors (last rule)]
    (if-not (or (string? errors) (and (map? errors) (:default errors)))
      (error path "Last element of a rule must be the error message string or map with a :default key."))

    ))



(defn validate-string-condition [path condition]
  (cond (and (keyword? condition) (in? [:required] condition))
        true

        (= (str (class condition)) "class java.util.regex.Pattern")
        true


        (and (vector? condition) (= (first condition) :length))
        (if (= (count condition) 3) true
          (error path "Invalid :length condition"))


        ; cross key validation
        (and (vector? condition) (fn? (first condition)))
        true ; todo add more structural validation for cross key conditions


;;         ; cross key validation
;;         (and (vector? condition) (= (first condition) :matches))
;;         true ; todo add more structural validation for cross key conditions


        :else
        (error path (str "Invalid condition: " condition))))


(defn validate-int-condition [path condition]
  (cond (and (keyword? condition) (in? [:required] condition))
        true

        (and (vector? condition) (= (first condition) :range))
        (if (= (count condition) 3) true
          (error path "Invalid :range condition"))


        ; cross key validation
        (and (vector? condition) (fn? (first condition)))
        true ; todo add more structural validation for cross key conditions



        :else
        (error path (str "Invalid condition: " condition))))


(defn validate-datetime-condition [path condition]
  (cond (and (keyword? condition) (in? [:required] condition))
        true


        ; cross key condition
        (and (vector? condition) (in? [:before :after] (first condition)))
        (if (>= (count condition) 2) true
             (error path (str "Invalid condition: " condition)))


        :else
        (error path (str "Invalid condition: " condition))))




; (validate-datetime-condition [:b] [:after :a])



(defn validate-email-condition [path condition]
  (cond (and (keyword? condition) (in? [:required] condition))
        true


        :else
        (error path (str "Invalid condition: " condition))))



(defn validate-custom-condition [path condition]

  ; check arity
  (let [m (first (filter #(= "invoke" (.getName %)) (.getDeclaredMethods (class condition))))
        p (.getParameterTypes m)]

    (if (= 3 (alength p)) true
      (error path (str "Custom conditions must have arity of 3. (fn [culure value] ... )")))

    ))


(defn validate-condition
  "Validates a supplied raw rule condition

  (validate-condition :string :id [:length 3 10])
  => true

  (validate-condition :string :id [:length 3])
  => java.lang.Exception: Error found in the rule for :id. Invalid :length condition
  "
  [path datatype condition]

  (cond
   (fn? condition)         (validate-custom-condition    path condition)
   (= datatype :string)    (validate-string-condition    path condition)
   (= datatype :int)       (validate-int-condition       path condition)
   (= datatype :datetime)  (validate-datetime-condition  path condition)
   (= datatype :email)  (validate-email-condition     path condition)

   :else (throw (Exception.
                 (error path
                        (str path " " condition " is not a suppored condition"))))))



(defn validate-rule-conditions [path datatype conditions]
  (doall (for [c conditions]
    (validate-condition path datatype c))))





