
; General validation process

; 1. Entire raw map is parsed
; 2. Conditions on each checked for each field
; 3. Multi-field conditions are checked for entire parsed map
; 4. final results are packaged up with any error messages




; a simpler internal validator fn organization?
; currently each key has a single function which returns

{:name [(fn [raw culture] "")    ; first function always parses the data
        (fn [raw culture] "")    ; a seperate function for each condition

        ; multi-field conitions that need access to original map...
        ; they really would need access to the entire "parsed" map
        ; because for instance datetime conditions need parsed values to compare too.

        ; custom conditions? That need access to original map?
        ; would it be the "parsed" map or raw?

        {:default "error msg"}   ; last element is always error message
        ]}


; simple rule
{:name [:string :required "error"]
 :age  [:int :required "error"]}

; we then compile that into a model


; to support cross field validations, we then must parse entire model.
; if we need to parse entire model, then we can't have
; a single function that combines parsing, condition checks and error determination,
; therefore the model must seperate these I think.

{:name {:nodetype :field
        :parse (fn [raw] "parses raw into correct type")
        :conditions [fn1 fn2 fn3]
        :messages {:default "error msg"}}


 ; if the model must support nested rules we can't key off of map datatypes.
 ; how about using a :nodetype of :field to specify it's a node
 :address {:street {:nodetype :field
                    :parse (fn [raw] "parses raw into correct type")
                    :conditions [fn1 fn2 fn3]
                    :messages {:default "error msg"}}}
 }

; or we could make the model look like this

{:parse (fn [raw-map culture]    "returns the entire map parsed!(need culture for dates)")

 :check (fn [parsed-map culture] "checks all conditions and returns error (need culture for messages)")}


; or perhaps


; check
    ; using the model, parses all keys in model + raw-data into a parsed-result
    ; tests all conditions on all keys into a validated-result
    ; tests all multi-field conditions
    ; consolidates all parsed data and condition errors into result









; what syntax/methods for cross/multi field validations

{:password [:string :required "Please enter a password"]
 :confirm  [:string :required [:* = :password] "Passwords don't match"]

 :start    [:datetime :required "Please enter a start date"]
 :end      [:datetime :required [:* > :start] "End date is required and must be after start"]


 :*        [(fn [culture parsed-values]
              (if-not (= (:password parsed-values) (confirm values))
                "Passwords don't match"))
            :password :confirm]


 :*        [[fn1 :x]
            [fn2 :y]]

 }

=>

{:* ["Passwords don't match"]}




{:start [:datetime :required "Please enter a start date"]
 :end   [:datetime :required [:ref :start :after] "end must be after start"]

 :*     [[= :password :confirm "Password confirmation does not match password"]
         [>= :end :start "End date must be after start date"]]}




; nested/multi-level validation
{:name              [:string :required "Please enter a name"]
 :address {:street  [:string :required "Please enter a street address"]
           :zip     [:postcode :required "Zip code is required"]}}

{:name              (fn [] "name fn")
 :address {:street  (fn [] "street fn")
           :zip     (fn [] "zip fn")}}


{:address {:street ""}}


; collection validation
