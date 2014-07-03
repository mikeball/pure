(ns taoclj.pure.compilation
  (:require [taoclj.pure.util :refer [in?]]))


(defn error [field message]
  (throw (Exception. (str "Error found in the rule for " field ". " message))))



(defmulti validate-condition
  "Validates a supplied raw rule condition

  (validate-condition :string :id [:length 3 10])
  => true

  (validate-condition :string :id [:length 3])
  => java.lang.Exception: Error found in the rule for :id. Invalid :length condition
  "

  (fn [datatype field condition]
    (cond (fn? condition) :custom
          :else datatype)))


(defmethod validate-condition :default [datatype field condition]
  (throw (Exception. (error field (str datatype " " condition " is not a suppored condition")))))

(defmethod validate-condition :custom [_ _ condition]
  ; check arity
  (let [m (first (filter #(= "invoke" (.getName %)) (.getDeclaredMethods (class condition))))
        p (.getParameterTypes m)]
    (= 2 (alength p))))

(defmethod validate-condition :string [_ field condition]

  (cond (and (keyword? condition) (in? [:required] condition))
        true

        (= (str (class condition)) "class java.util.regex.Pattern")
        true

        (and (vector? condition) (= (first condition) :length))
        (if (= (count condition) 3) true
          (error field "Invalid :length condition"))

        :else
        (error field (str "Invalid condition: " condition))

        ))
; (validate-condition :string :id #"a")


(defmethod validate-condition :int [_ field condition]

  (cond (and (keyword? condition) (in? [:required] condition))
        true

        (and (vector? condition) (= (first condition) :range))
        (if (= (count condition) 3) true
          (error field "Invalid :range condition"))


        :else
        (error field (str "Invalid condition: " condition))

        ))


(defn read-rule
  "Validates and reads a rule into an easier to use form.

  (read-rule :id [:string :required [:length 2 3] \"error msg\"])
  => {:type :string
      :required true
      :conditions ([:length 2 3])
      :messages {:default \"error msg\"}}
  "

  [field rule]

  (let [length (count rule)]

    (if (< length 2)
      (error field "Rules must have at least 2 elements, with the first being a supported data-type and the last being the error message."))

    (let [t (first rule)
          m (last rule)
          all-conditions (-> rule rest drop-last)
          required (in? all-conditions :required)
          conditions (if (> length 2) (filter #(not= :required %) all-conditions))]

      (if-not (in? [:int :string :email] t)
        (error field
               "Invalid type in first element of rule. Allowed types are :int, :string and :email."))

      (if-not (or (string? m) (and (map? m) (:default m)))
        (error field "Last element of a rule must be the error message string or map with a :default key."))


      (doall (for [c conditions]
        (validate-condition t field c)))


      (-> {:type t
           :messages (if (string? m) {:default m} m)}

            ; if required, add required key
            ((fn [rr] (if required
                        (assoc rr :required required)
                        rr)))

            ; if there are conditions, add conditions key
            ((fn [rr conditions] (if-not (empty? conditions)
                        (assoc rr :conditions conditions)
                        rr)) conditions)
            ))))


; (read-rule :id [:string #"z" "e*"])



