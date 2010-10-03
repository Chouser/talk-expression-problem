(defn purchase-order [id date amount]
  ^{:type ::PurchaseOrder} {:id id, :date date, :amount amount})

(def test-purchase-orders
  [(purchase-order 1 "2010-01-01" 12.99)
   (purchase-order 2 "2010-02-02" 24.98)
   (purchase-order 3 "2010-03-03" 18.85)])


(defn inventory-item [description quantity cost]
  ^{:type ::InventoryItem} {:description description, :quantity quantity,
                            :cost cost})

(def test-inventory-items
  [(inventory-item "Widget" 28 5.00)
   (inventory-item "Gizmo"  32 6.00)
   (inventory-item "Thingy" 38 0.98)])


(defn html-report [data]
  (println "<table>")
  (println "  <tr>")
  (doseq [column-name (get-column-names (first data))]
    (println (str "    <th>" column-name "</th>")))
  (println "  </tr>")
  (doseq [row data]
    (println "  <tr>")
    (doseq [column-name (get-column-names row)]
      (println (str "    <td>" (get-value row column-name) "</td>")))
    (println "  </tr>"))
  (println "</table>"))


;;----

(defmulti get-column-names type)
(defmulti get-value (fn [this column-name] (type this)))

(defmethod get-column-names ::PurchaseOrder [_]
  ["id" "date" "amount"])

(defmethod get-value ::PurchaseOrder [this column-name]
  (get this (keyword column-name)))

(defn format-money [x]
  (format "%.2f" x))

(defmethod get-column-names ::InventoryItem [_]
  ["description" "quantity" "unit cost" "total cost"])

(defmethod get-value ::InventoryItem [this column-name]
  (case column-name
    "description" (:description this)
    "quantity"    (:quantity this)
    "unit cost"   (format-money (:cost this))
    "total cost"  (format-money (* (:cost this) (:quantity this)))))


;;---- solution #3: multi-methods

(def multiplication-tables
  [[0  0  0  0  0]
   [1  2  3  4  5]
   [2  4  6  8 10]
   [3  6  9 12 15]])

(defmethod get-column-names clojure.lang.Associative [this]
  (range (count this)))

(defmethod get-value clojure.lang.Associative [this column-name]
  (nth this column-name))


;; wouldn't it be nice if...

(def-type-multis DataRow
  (get-column-names [this])
  (get-value [this column-name]))

(defmethods DataRow
  clojure.lang.Associative
  (get-column-names [this]
    (range (count this)))
  (get-value [this column-name]
    (nth this column-name)))

;; so make it happen...

(defn type* [obj & _] (type obj))

(defmacro def-type-multis [name & methods]
  `(do
     ~@(for [[method-name] methods]
         `(defmulti ~method-name type*))))

(defmacro defmethods [name dispatch-value & methods]
  `(do
     ~@(for [[method-name & more] methods]
         `(defmethod ~method-name ~dispatch-value ~more))))

;; solution #4: protocols

(defprotocol DataRow
  (get-column-names [this])
  (get-value [this column-name]))

(extend-protocol DataRow
  clojure.lang.Associative
  (get-column-names [this]
    (range (count this)))
  (get-value [this column-name]
    (nth this column-name)))


(defrecord PurchaseOrder [id date amount])

;; Identical implementations:
(extend-protocol DataRow
  (get-column-names [this]
    ["id" "date" "amount"])
  (get-value [this column-name]
    (get this (keyword column-name))))

(def test-purchase-orders
  [(PurchaseOrder. 1 "2010-01-01" 12.99)
   (PurchaseOrder. 2 "2010-02-02" 24.98)
   (PurchaseOrder. 3 "2010-03-03" 18.85)])

(defrecord InventoryItem [description quantity cost])

(extend-protocol DataRow
  (get-column-names [this]
    ["description" "quantity" "unit cost" "total cost"])
  (get-value [this column-name]
    (case column-name
      "description" (:description this)
      "quantity"    (:quantity this)
      "unit cost"   (format-money (:cost this))
      "total cost"  (format-money (* (:cost this) (:quantity this))))))

(def test-inventory-items
  [(InventoryItem. "Widget" 28 5.00)
   (InventoryItem. "Gizmo"  32 6.00)
   (InventoryItem. "Thingy" 39 0.98)])
