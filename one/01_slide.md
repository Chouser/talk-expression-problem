!SLIDE bullets

# Clojure’s Solutions to the Expression Problem

* Chris Houser
* a.k.a. Chouser
* Strange Loop, Oct. 14 2010, St. Louis

!SLIDE bullets transition=scrollLeft

# The Plan

* Demonstrate the Expression Problem.
* Two ways some languages solve it.
* Using Clojure multimethods to solve it.
* Using Clojure protocols to solve it.
* Review the pros and cons of each solution.

!SLIDE center transition=scrollLeft
<!--<embed src="image/one/media/expression-problems.svg" width="1024" height="768" type="image/svg+xml" />-->
<img src="media/expression-problems.png" width="991" height="730" />

!SLIDE transition=scrollLeft
# Simple business objects
<embed src="image/one/media/example-flow.svg" width="1024" height="768" type="image/svg+xml" />

!SLIDE transition=scrollLeft

# Strawman `PurchaseOrder`

    @@@ strawman
    public class PurchaseOrder {
      public id; public date; public amount;

      public PurchaseOrder(id, date, amount) {
        this.id = id; this.date = date;
        this.amount = amount;
      }

      public static getTestData() {
        data = new Vector();
        data.add(new PurchaseOrder(1,"2010-01-01",12.99));
        data.add(new PurchaseOrder(2,"2010-02-02",24.98));
        data.add(new PurchaseOrder(3,"2010-03-03",18.85));
        return data;
      }
    }

!SLIDE transition=scrollLeft

# Strawman `InventoryItem`

    @@@ strawman
    public class InventoryItem {
      public description; public quantity; public cost;

      public InventoryItem(description, quantity, cost) {
        this.description = description;
        this.quantity = quantity;
        this.cost = cost;
      }

      public static getTestData() {
        data = new Vector();
        data.add(new InventoryItem("Widget", 28, 5.00));
        data.add(new InventoryItem("Gizmo",  32, 6.00));
        data.add(new InventoryItem("Thingy", 38, 0.98));
        return data;
    } }

!SLIDE transition=scrollLeft

# Strawman `HtmlReport`

    @@@ strawman
    public class HtmlReport {
      public static printReport(data) {
        Sys.print("<table><tr>");       // table header
        for(col in data[0].getColumnNames()) {
          Sys.print("<th>", col, "</th>");
        }
        Sys.print("</tr>");
        for(row in data) {              // table body
          Sys.print("<tr>");
          for(col in row.getColumnNames()) {
            Sys.print("<td>",row.getValue(col),"</td>");
          }
          Sys.print("</tr>");
        }
        Sys.print("</table>");
    } }

!SLIDE bullets transition=scrollLeft

# DataRow Abstraction

* getColumnNames()
* getValue(columnName)

!SLIDE transition=scrollLeft

# Strawman `DataRow`

    @@@ strawman
    abstract class DataRow {
      public getColumnNames();
      public getValue(columnName);
    }

!SLIDE transition=scrollLeft
# Missing connection
<embed src="image/one/media/example-flow2.svg" width="1024" height="768" type="image/svg+xml" />

!SLIDE transition=scrollLeft

# `PurchaseOrder implements DataRow`

    @@@ strawman
    public class PurchaseOrder implements DataRow {
      ...
      public getColumnNames() {
        cols = new Vector();
        cols.add("id");
        cols.add("date");
        cols.add("amount");
        return cols;
      }

      public getValue(col) {
        if(col == "id")          return this.id;
        else if(col == "date")   return this.date;
        else if(col == "amount") return this.amount;
      }
    }

!SLIDE transition=scrollLeft

# `InventoryItem implements DataRow`

    @@@ strawman
    public class InventoryItem implements DataRow {
      ...
      public getColumnNames() {
        cols = new Vector();
        cols.add("description"); cols.add("quantity");
        cols.add("unit cost");   cols.add("total cost");
        return cols;
      }
      public getValue(col) {
        if(col == "description")   return this.description;
        else if(col == "quantity") return this.quantity;
        else if(col == "cost")     return this.cost;
        else if(col == "total cost")
          return (this.cost * this.quantity);
      }
    }

!SLIDE bullets incremental transition=scrollLeft

# The problem...

* “A vector is like a row of data with numbers for the column names”
* Implement `DataRow` for the built-in class `Vector`

!SLIDE center transition=scrollLeft

<embed src="image/one/media/ludacris.svg" width="1024" height="768" type="image/svg+xml" />

!SLIDE bullets transition=scrollLeft

# Solution 1: Adapters

    @@@strawman
    public class VectorDataRowAdapter implements DataRow {
      public vector;

      public VectorDataRowAdapter(vector) {
        this.vector = vector;
      }
      public getColumnNames() {
        cols = new Vector();
        for(i = 0; i < this.vector.count(); ++i) {
          cols.add(i);
        }
        return cols;
      }
      public getValue(columnName) {
        return this.vector[columnName];
    } }

!SLIDE bullets incremental transition=scrollLeft

# Adapter drawbacks

* Different type of object -- no longer a vector
* Proxy vector methods so it behaves like one?
* What about equality?
* How to handle deeply-nested objects?

!SLIDE bullets transition=scrollLeft

# Nested vectors

    @@@strawman
      static public wrapTable(table) {
        out = new Vector();
        for(row = 0; row < table.count(); ++row) {
          out.add(new VectorDataRowAdapter(table[row]));
        }
        return out;
      }

!SLIDE bullets transition=scrollLeft

# Solution 2: Monkey patch

<embed src="image/one/media/monkey-patch.svg" width="1024" height="768" type="image/svg+xml" />

!SLIDE bullets transition=fade

# Solution 2: Monkey patch

    @@@strawman
    patch class Vector implements DataRow {

      public getColumnNames() {
        cols = new Vector();
        for(i = 0; i < this.count(); ++i) {
          cols.add(i);
        }
        return cols;
      }

      public getValue(columnName) {
        return this[columnName];
      }
    }

!SLIDE bullets transition=scrollLeft

# Monkey patch drawback

* Method name clashes within the class

.notes foo!

    @@@strawman


    myVector.getValue(x);  // which "getValue"?

!SLIDE bullets transition=scrollLeft

# Clojure business objects

    @@@ clojure
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

!SLIDE bullets transition=scrollLeft

# Clojure `html-report`

    @@@ clojure
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

!SLIDE bullets transition=scrollLeft

# Solution 3

<embed src="image/one/media/the-multiple-dispatch.svg" width="1024" height="768" type="image/svg+xml" />

!SLIDE transition=scrollLeft

# Abstraction used<br />by `html-report`

    @@@ clojure
    ; Abstraction, like DataRow:
    (defmulti get-column-names (fn [this] (type this)))
    (defmulti get-value    (fn [this col] (type this)))

!SLIDE bullets transition=scrollLeft

# Connect `PurchaseOrder`<br />to `html-report`

    @@@ clojure
    ; Implement abstraction for PurchaseOrder
    (defmethod get-column-names ::PurchaseOrder [_]
      ["id" "date" "amount"])

    (defmethod get-value ::PurchaseOrder [this col]
      (get this (keyword col)))

!SLIDE bullets transition=scrollLeft

# Connect `InventoryItem`<br />to `html-report`

    @@@ clojure
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

!SLIDE bullets transition=scrollLeft

# Connect `Vector`<br />to `html-report`
.notes (import 'clojure.lang.Associative)

    @@@clojure
    (defmethod get-column-names Associative [this]
      (range (count this)))
    
    (defmethod get-value Associative [this column-name]
      (get this column-name))

    (def multiplication-table
      [[1  2  3  4  5]
       [2  4  6  8 10]
       [3  6  9 12 15]
       [4  8 12 16 20]])

!SLIDE bullets transition=scrollLeft
      
# Multimethod namespacing

* All methods exist in *declaring* namespace, not *target-type's* namespace

.notes whatever

    @@@clojure
    (ns chouser.reporter)
    (defmulti get-value ...)

    (ns other.interloper)
    (defmulti get-value ...)

    (chouser.reporter/get-value ...)
    (other.interloper/get-value ...)

!SLIDE bullets incremental transition=scrollLeft

# Multimethod drawback

* Performance

!SLIDE bullets transition=scrollLeft

# Solution 4: Protocols

<embed src="image/one/media/protocol.svg" width="1024" height="768" type="image/svg+xml" />

!SLIDE bullets transition=scrollLeft

# Records

* Protocols dispatch on an object's **class**

.notes bar!

    @@@clojure
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

!SLIDE bullets transition=scrollLeft

# `DataRow` Protocol, extended to `Associative`

    @@@clojure
    (defprotocol DataRow
      (get-column-names [this])
      (get-value [this column-name]))
    
    (extend-protocol DataRow
      clojure.lang.Associative
      (get-column-names [this]
        (range (count this)))
      (get-value [this column-name]
        (get this column-name)))

!SLIDE bullets transition=scrollLeft

# `DataRow` Protocol, extended to `PurchaseOrder`

    @@@clojure
    (extend-protocol DataRow
      PurchaseOrder
      (get-column-names [this]
        ["id" "date" "amount"])
      (get-value [this column-name]
        (get this (keyword column-name))))

!SLIDE bullets transition=scrollLeft

# `DataRow` Protocol, extended to `InventoryItem`

    @@@clojure
    (extend-protocol DataRow
      InventoryItem
      (get-column-names [this]
        ["description" "quantity" "unit cost" "total cost"])
      (get-value [this column-name]
        (case column-name
          "description" (:description this)
          "quantity"    (:quantity this)
          "unit cost"   (format-money (:cost this))
          "total cost"  (format-money
                          (* (:cost this) (:quantity this))))))

!SLIDE bullets incremental transition=scrollLeft

# Protocol drawbacks

* Can only dispatch on *class* of object

!SLIDE bullets incremental transition=scrollLeft

# Moving from Multimethods to Protocols

* Change constructors/factories
* Move implementations from `defmethod` to `extend`
* No change at all to users of the multimethod/protocol functions

!SLIDE bullets transition=scrollLeft

# The Expression Problem

* ...is when you need to implement an abstraction for an *existing*, hard-to-modify concrete object.

# Can be solved using:

* Adapter<br/>Monkey patch<br/>Multimethod<br/>Protocol

!SLIDE bullets transition=scrollLeft

# Solutions

* **Adapter**<br/>has equality, identity problems
* **Monkey patch**<br/>has method-name collision problems
* **Multimethod**<br/>is a good solution and very flexible
* **Protocol**<br/>is fastest; be aware of design implications

!SLIDE bullets transition=scrollLeft

* Have you experienced the expression problem? Which solution did you use?
* Did you experience the<br/>drawbacks of that solution?
* Which of the other solutions<br/>are available in your language?
* Would their drawbacks have been<br/>more desirable in your case?
* Would either of Clojure's<br/>solutions been a better fit?

!SLIDE bullets transition=scrollLeft

# Any other questions?

<embed src="image/one/media/cover.svg" width="1024" height="768" type="image/svg+xml" />
