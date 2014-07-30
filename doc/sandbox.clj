

; what syntax/methods for top level validations

{:*        [(fn [values culture]
              (if-not (= (:password parsed-values) (confirm values))
                "Passwords don't match"))
            :password :confirm]

 :*        [[fn1 :x]
            [fn2 :y]]

 }

=> {:* ["Passwords don't match"]}




; Handle additional information passed to custom validation conditions
; specifically the need to reference the current userid in a query.
(db/q {:select "COUNT(*)"
       :from "users"
       :where (format "login = '%s' AND id <> %d" value (:id subject))






; *** collection validation ?
