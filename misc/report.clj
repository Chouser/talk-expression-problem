(ns chouser.reporter
  (:import (clojure.lang Associative)))

(defn purchase-order [id date amount]
  ^{:type ::PurchaseOrder} {:id id, :date date,
                            :amount amount})
(def test-purchase-orders
  [(purchase-order 1 "2010-01-01" 12.99)
   (purchase-order 2 "2010-02-02" 24.98)
   (purchase-order 3 "2010-03-03" 18.85)])

(defn inventory-item [description quantity cost]
  ^{:type ::InventoryItem} {:description description,
                            :quantity quantity,
                            :cost cost})
(def test-inventory-items
  [(inventory-item "Widget" 28 5.00)
   (inventory-item "Gizmo"  32 6.00)
   (inventory-item "Thingy" 38 0.98)])

(defmulti get-column-names (fn [this] (type this)))
(defmulti get-value    (fn [this col] (type this)))

(defn html-report [data]
  (println "<table>")
  (println "  <tr>")
  (doseq [column-name (get-column-names (first data))]
    (println (str "    <th>" column-name "</th>")))
  (println "  </tr>")
  (doseq [row data]
    (println "  <tr>")
    (doseq [column-name (get-column-names row)]
      (println (str "    <td>"
                    (get-value row column-name)
                    "</td>")))
    (println "  </tr>"))
  (println "</table>"))

(defmethod get-column-names ::PurchaseOrder [_]
  ["id" "date" "amount"])

(defmethod get-value ::PurchaseOrder [this col]
  (get this (keyword col)))

(html-report test-purchase-orders)

(defn format-money [x] (format "%.2f" x))

(defmethod get-column-names ::InventoryItem [_]
  ["description" "quantity" "unit cost" "total cost"])

(defmethod get-value ::InventoryItem [this col]
  (case col
    "description"(:description this)
    "quantity"   (:quantity this)
    "unit cost"  (format-money (:cost this))
    "total cost" (format-money
                  (* (:cost this) (:quantity this)))))

(html-report test-inventory-items)

(defmethod get-column-names Associative [this]
  (range (count this)))

(defmethod get-value Associative [this column-name]
  (get this column-name))

(def multiplication-table
  [[1  2  3  4  5]
   [2  4  6  8 10]
   [3  6  9 12 15]
   [4  8 12 16 20]])

(html-report multiplication-table)

(defrecord PurchaseOrder [id date amount])

(def test-purchase-orders
  [(PurchaseOrder. 1 "2010-01-01" 12.99)
   (PurchaseOrder. 2 "2010-02-02" 24.98)
   (PurchaseOrder. 3 "2010-03-03" 18.85)])

(defrecord InventoryItem [description quantity cost])

(def test-inventory-items
  [(InventoryItem. "Widget" 28 5.00)
   (InventoryItem. "Gizmo"  32 6.00)
   (InventoryItem. "Thingy" 39 0.98)])

(defprotocol DataRow
  (get-column-names [this])
  (get-value [this column-name]))

(extend-protocol DataRow
  clojure.lang.Associative
  (get-column-names [this]
    (range (count this)))
  (get-value [this column-name]
    (get this column-name)))

(html-report multiplication-table)

(extend-protocol DataRow
  PurchaseOrder
  (get-column-names [this]
    ["id" "date" "amount"])
  (get-value [this column-name]
    (get this (keyword column-name))))

(html-report test-purchase-orders)

(extend-protocol DataRow
  InventoryItem
  (get-column-names [this]
    ["description" "quantity" "unit cost" "total cost"])
  (get-value [this column-name]
    (case column-name
      "description" (:description this)
      "quantity"    (:quantity this)
      "unit cost"   (format-money (:cost this))
      "total cost"  (format-money (* (:cost this) (:quantity this))))))

(html-report test-inventory-items)
