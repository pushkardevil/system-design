import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// Enums for vehicle and spot type/status
enum VehicleType { COMPACT, REGULAR, LARGE }
enum SpotStatus { FREE, RESERVED, OCCUPIED }

class User {
    int id;
    String email, password, firstName, lastName;
    public User(int id, String email, String password, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}

class Vehicle {
    int id, userId;
    String license;
    VehicleType type;
    public Vehicle(int id, int userId, String license, VehicleType type) {
        this.id = id;
        this.userId = userId;
        this.license = license;
        this.type = type;
    }
}

class Spot {
    int id, garageId;
    VehicleType type;
    SpotStatus status;
    public Spot(int id, int garageId, VehicleType type) {
        this.id = id;
        this.garageId = garageId;
        this.type = type;
        this.status = SpotStatus.FREE;
    }
}

class Garage {
    int id;
    String zipcode;
    double rateCompact, rateRegular, rateLarge;
    List<Spot> spots = new ArrayList<>();
    public Garage(int id, String zipcode, double rc, double rr, double rl) {
        this.id = id;
        this.zipcode = zipcode;
        this.rateCompact = rc;
        this.rateRegular = rr;
        this.rateLarge = rl;
    }
}

class Reservation {
    int id, garageId, spotId, userId;
    LocalDateTime start, end;
    boolean paid;
    public Reservation(int id, int garageId, int spotId, int userId,
                       LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.garageId = garageId;
        this.spotId = spotId;
        this.userId = userId;
        this.start = start;
        this.end = end;
        this.paid = false;
    }
}

class ParkingSystem {
    private final List<User> users = new ArrayList<>();
    private final List<Garage> garages = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final AtomicInteger userIdSeq = new AtomicInteger(1);
    private final AtomicInteger garageIdSeq = new AtomicInteger(1);
    private final AtomicInteger spotIdSeq = new AtomicInteger(1);
    private final AtomicInteger reservationIdSeq = new AtomicInteger(1);
    private final AtomicInteger vehicleIdSeq = new AtomicInteger(1);

    // Account endpoints
    public User createAccount(String email, String password, String first, String last) {
        User u = new User(userIdSeq.getAndIncrement(), email, password, first, last);
        users.add(u);
        return u;
    }
    public boolean login(String email, String password) {
        return users.stream().anyMatch(u -> u.email.equals(email) && u.password.equals(password));
    }

    // Garage and spot management
    public Garage createGarage(String zipcode, double c, double r, double l) {
        Garage g = new Garage(garageIdSeq.getAndIncrement(), zipcode, c, r, l);
        garages.add(g);
        return g;
    }
    public Spot addSpot(Garage g, VehicleType type) {
        Spot s = new Spot(spotIdSeq.getAndIncrement(), g.id, type);
        g.spots.add(s);
        return s;
    }
    public Vehicle addVehicle(int userId, VehicleType type, String license) {
        Vehicle v = new Vehicle(vehicleIdSeq.getAndIncrement(), userId, license, type);
        vehicles.add(v);
        return v;
    }

    // Reservation — NO double booking allowed
    private Optional<Spot> findAvailableSpot(int garageId, VehicleType vtype,
                                             LocalDateTime start, LocalDateTime end) {
        for (Garage g : garages) {
            if (g.id != garageId) continue;
            for (Spot spot : g.spots) {
                if (!(spot.type == vtype || spot.type == VehicleType.LARGE)) continue;
                if (spot.status != SpotStatus.FREE) continue;

                boolean overlap = false;
                for (Reservation r : reservations) {
                    if (r.spotId == spot.id &&
                        !(end.isBefore(r.start) || start.isAfter(r.end))) {
                        overlap = true; break;
                    }
                }
                if (!overlap) return Optional.of(spot);
            }
        }
        return Optional.empty();
    }

    // /reserve
    public Optional<Integer> reserve(int garageId, VehicleType vtype, int userId,
                                     LocalDateTime start, LocalDateTime end) {
        Optional<Spot> sOpt = findAvailableSpot(garageId, vtype, start, end);
        if (sOpt.isEmpty()) return Optional.empty();
        Spot s = sOpt.get();
        s.status = SpotStatus.RESERVED;
        Reservation r = new Reservation(reservationIdSeq.getAndIncrement(),
                garageId, s.id, userId, start, end);
        reservations.add(r);
        return Optional.of(r.id);
    }

    // /payment
    public boolean payment(int reservationId) {
        for (Reservation r : reservations) {
            if (r.id == reservationId) {
                r.paid = true;
                return true;
            }
        }
        return false;
    }

    // /cancel
    public boolean cancel(int reservationId) {
        for (Reservation r : reservations) {
            if (r.id == reservationId) {
                for (Garage g : garages)
                    for (Spot s : g.spots)
                        if (s.id == r.spotId) s.status = SpotStatus.FREE;
                r.start = r.end; // zero length
                r.paid = false;
                return true;
            }
        }
        return false;
    }

    // /calculate_payment
    public double calculatePayment(int reservationId) {
        for (Reservation r : reservations) {
            if (r.id == reservationId) {
                Garage g = garages.stream().filter(gar -> gar.id == r.garageId).findFirst().orElse(null);
                Spot s = null;
                if (g != null)
                    s = g.spots.stream().filter(sp -> sp.id == r.spotId).findFirst().orElse(null);
                if (g != null && s != null) {
                    double rate = switch (s.type) {
                        case COMPACT -> g.rateCompact;
                        case REGULAR -> g.rateRegular;
                        case LARGE -> g.rateLarge;
                    };
                    long duration = Duration.between(r.start, r.end).toHours();
                    return rate * duration;
                }
            }
        }
        return -1;
    }

    // /freespots
    public List<Spot> freeSpots(int garageId, VehicleType vtype, LocalDateTime now) {
        List<Spot> result = new ArrayList<>();
        for (Garage g : garages) {
            if (g.id != garageId) continue;
            for (Spot s : g.spots)
                if (s.type == vtype && s.status == SpotStatus.FREE)
                    result.add(s);
        }
        return result;
    }

    // Print reservations for demonstration
    public void printReservations() {
        System.out.println("Reservations:");
        for (Reservation r : reservations) {
            System.out.printf("ID: %d, Spot: %d, Paid: %b, Start: %s, End: %s%n",
                    r.id, r.spotId, r.paid, r.start, r.end);
        }
    }
}

// ---------- Your main entry point (compile as Main.java) ----------
public class Main {
    public static void main(String[] args) {
        ParkingSystem sys = new ParkingSystem();

        // Create garage and spots
        Garage g = sys.createGarage("12345", 2.0, 3.0, 5.0);
        sys.addSpot(g, VehicleType.COMPACT);
        sys.addSpot(g, VehicleType.REGULAR);
        sys.addSpot(g, VehicleType.LARGE);

        // Create user, add vehicle
        User u = sys.createAccount("alice@mail.com", "pass", "Alice", "S");
        Vehicle v = sys.addVehicle(u.id, VehicleType.REGULAR, "DL8CAA1234");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime after2h = now.plusHours(2);

        Optional<Integer> ridOpt = sys.reserve(g.id, v.type, u.id, now, after2h);
        if (ridOpt.isPresent()) {
            int rid = ridOpt.get();
            System.out.println("Reserved! Reservation ID: " + rid);
            double cost = sys.calculatePayment(rid);
            System.out.println("Cost: " + cost);
            sys.payment(rid);
        } else {
            System.out.println("No available spots!");
        }

        sys.printReservations();
    }
}
