

; what syntax/methods for cross/multi field validations

{:password [:string :required "Please enter a password"]
 :confirm  [:string :required [:* = :password] "Passwords don't match"]

 :start    [:datetime :required "Please enter a start date"]
 :end      [:datetime :required [:* > :start] "End date is required and must be after start"]


 :*        [(fn [values culture]
              (if-not (= (:password parsed-values) (confirm values))
                "Passwords don't match"))
            :password :confirm]


 :*        [[fn1 :x]
            [fn2 :y]]

 }

=> {:* ["Passwords don't match"]}



{:start [:datetime :required "Please enter a start date"]
 :end   [:datetime :required [:ref :start :after] "end must be after start"]

 :*     [[= :password :confirm "Password confirmation does not match password"]
         [>= :end :start "End date must be after start date"]]}




; Handle additional information passed to custom validation conditions
; specifically the need to reference the current userid in a query.
(db/q {:select "COUNT(*)"
       :from "users"
       :where (format "login = '%s' AND id <> %d" value (:id subject))



; datetime range condition
; :range condition just like int?
; [:range start end]

; or this? [:datetime "yyyy-MM-dd" [:before :hours -10] "e*"]





; *** collection validation
