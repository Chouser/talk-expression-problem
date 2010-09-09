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

(defmethod get-column-names ::InventoryItem [_]
  ["description" "quantity" "unit cost" "total cost"])

(defmethod get-value ::InventoryItem [this column-name]
  (case column-name
    "description" (:description this)
    "quantity"    (:quantity this)
    "unit cost"   (:cost this)
    "total cost"  (* (:cost this) (:quantity this))))

