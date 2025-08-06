import java.util.*;

class Product {
    int id;
    String name;
    double price;
    String description;

    public Product(int id, String name, double price, String description) {
        this.id = id; this.name = name; this.price = price; this.description = description;
    }
}

class Slot {
    int id;
    Product product;
    int quantity;
    int position;

    public Slot(int id, Product product, int quantity, int position) {
        this.id = id; this.product = product; this.quantity = quantity; this.position = position;
    }
}

enum PaymentType {
    CASH, CARD, QR
}

class Payment {
    PaymentType paymentType;
    double amount;
    boolean status = false;

    public Payment(PaymentType pt, double amount) {
        this.paymentType = pt; this.amount = amount;
    }
}

class Transaction {
    int id;
    List<Slot> items = new ArrayList<>();
    Payment payment;
    double totalAmount = 0;
    String status = "PENDING";

    public Transaction(int id) { this.id = id; }

    public void addItem(Slot slot, int count) {
        items.add(slot);
        totalAmount += slot.product.price * count;
    }

    public void completeTransaction() { status = "COMPLETED"; }
}

class VendingMachine {
    Map<Integer, Slot> slots = new HashMap<>();
    double cashBox = 0.0;

    public void addSlot(Slot slot) { slots.put(slot.position, slot); }

    public void restock(int slotId, int qty) {
        Slot slot = slots.get(slotId);
        if (slot != null) slot.quantity += qty;
    }

    public boolean dispenseProduct(int slotId) {
        Slot slot = slots.get(slotId);
        if (slot != null && slot.quantity > 0) {
            slot.quantity--;
            System.out.println("Product dispensed");
            return true;
        }
        System.out.println("Out of stock");
        return false;
    }
}

public class Main {
    public static void main(String[] args) {
        Product p1 = new Product(1, "Soda", 1.5, "Refreshing soda");
        Slot s1 = new Slot(1, p1, 10, 1);

        VendingMachine vm = new VendingMachine();
        vm.addSlot(s1);

        // Transaction simulation:
        vm.dispenseProduct(1);
    }
}
