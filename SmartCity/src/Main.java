// SmartCity.java
// 6 ta design pattern: Singleton + Facade + Abstract Factory + Builder + Decorator + Proxy
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {
    // ---------- 1) Singleton + Facade ----------
    public static class CityController {
        private static CityController instance;
        private final Map<String, Subsystem> subsystems = new HashMap<>();

        private CityController() {}

        public static synchronized CityController getInstance() {
            if (instance == null) instance = new CityController();
            return instance;
        }

        public void register(String name, Subsystem subsystem) {
            subsystems.put(name, subsystem);
        }

        // Facade metodlar — sodda interfeys
        public void turnOnAllLights() {
            LightingSubsystem ls = (LightingSubsystem) subsystems.get("lighting");
            if (ls != null) ls.turnOnAll();
        }

        public void emergencyStopTraffic() {
            TransportSubsystem ts = (TransportSubsystem) subsystems.get("transport");
            if (ts != null) ts.emergencyStop();
        }

        public String getEnergyReport() {
            EnergySubsystem es = (EnergySubsystem) subsystems.get("energy");
            if (es != null) return es.getReport();
            return "Energiya tizimi yo'q";
        }

        public void setEnergyMode(String mode) {
            EnergySubsystem es = (EnergySubsystem) subsystems.get("energy");
            if (es != null) es.setMode(mode);
        }
    }

    // ---------- Subsystem interfaces ----------
    public interface Subsystem { /* marker */ }

    public interface LightingSubsystem extends Subsystem {
        void turnOnAll();
    }

    public interface TransportSubsystem extends Subsystem {
        void emergencyStop();
    }

    public interface EnergySubsystem extends Subsystem {
        String getReport();
        void setMode(String mode);
    }

    // ---------- 3) Abstract Factory + Factory Method ----------
    public static class Device {
        private final String name;
        private final String status;

        public Device(String name, String status) {
            this.name = name;
            this.status = status;
        }

        public String getName() { return name; }
        public String getStatus() { return status; }
    }

    public interface DeviceFactory {
        Device createDevice();
    }

    public static class LightingFactory implements DeviceFactory {
        @Override
        public Device createDevice() {
            return new Device("Smart Chiroq", "Yoqilgan – 90% yorug'lik");
        }
    }

    public static class TransportFactory implements DeviceFactory {
        @Override
        public Device createDevice() {
            return new Device("Smart Svetofor", "YASHIL – harakat ruxsat");
        }
    }

    public static class DeviceFactoryProvider {
        public static DeviceFactory getFactory(String type) {
            switch (type) {
                case "lighting": return new LightingFactory();
                case "transport": return new TransportFactory();
                default: return null;
            }
        }
    }

    // ---------- 4) Builder pattern — Smart ko'cha qurish ----------
    public static class SmartStreet {
        private final int lamps;
        private final int cameras;
        private final int solarPanels;

        private SmartStreet(int lamps, int cameras, int solarPanels) {
            this.lamps = lamps;
            this.cameras = cameras;
            this.solarPanels = solarPanels;
        }

        public String info() {
            return String.format("Chiroq: %d ta | Kamera: %d ta | Quyosh paneli: %d ta",
                    lamps, cameras, solarPanels);
        }
    }

    public static class SmartStreetBuilder {
        private int lamps = 0;
        private int cameras = 0;
        private int solarPanels = 0;

        public SmartStreetBuilder addLamps(int n) { this.lamps = n; return this; }
        public SmartStreetBuilder addCameras(int n) { this.cameras = n; return this; }
        public SmartStreetBuilder addSolarPanels(int n) { this.solarPanels = n; return this; }

        public SmartStreet build() {
            return new SmartStreet(lamps, cameras, solarPanels);
        }
    }

    // ---------- 5) Decorator — logging qo'shish ----------
    // Biz oddiy FunctionalInterface ishlatamiz: Command (no-arg action)
    @FunctionalInterface
    public interface Command {
        void execute();
    }

    public static Command withLogging(Command cmd, String name) {
        return () -> {
            System.out.println("[LOG] " + name + " ishga tushdi");
            cmd.execute();
        };
    }

    // ---------- 6) Proxy — faqat admin o'zgartira oladi ----------
    public static class RealEnergySystem implements EnergySubsystem {
        private String mode = "normal";
        @Override
        public String getReport() {
            return String.format("Energiya rejimi: %s | Sarf: 312 kVt/soat", mode);
        }
        @Override
        public void setMode(String mode) {
            this.mode = mode;
            System.out.println("Energiya rejimi " + mode + " ga o'zgartirildi");
        }
    }

    // Proxy orqali rolni tekshiramiz
    public static class EnergySystemProxy implements EnergySubsystem {
        private final RealEnergySystem real;
        private final String role;

        public EnergySystemProxy(String role) {
            this.real = new RealEnergySystem();
            this.role = role;
        }

        @Override
        public String getReport() {
            return real.getReport();
        }

        @Override
        public void setMode(String mode) {
            if (!"admin".equalsIgnoreCase(role)) {
                System.out.println("XATO: Faqat ADMIN o'zgartira oladi!");
                return;
            }
            real.setMode(mode);
        }
    }

    // ---------- Main va konsol menyusi ----------
    public static void main(String[] args) {
        System.out.println("\n".repeat(2) + "SmartCity Tizimi ishga tushdi!\n");

        CityController controller = CityController.getInstance();

        // Subsystemlarni yaratamiz (lighting va transport uchun decorator bilan)
        Command lightsCommand = withLogging(() -> System.out.println("Barcha shahar chiroqlari YOQILDI"), "turnOnAllLights");
        LightingSubsystem lightingSystem = new LightingSubsystem() {
            @Override
            public void turnOnAll() {
                lightsCommand.execute();
            }
        };

        Command transportCommand = withLogging(() -> System.out.println("BARCHA TRANSPORT TO'XTATILDI!"), "emergencyStop");
        TransportSubsystem transportSystem = new TransportSubsystem() {
            @Override
            public void emergencyStop() {
                transportCommand.execute();
            }
        };

        // Energiyaga proxy (default guest)
        EnergySubsystem currentEnergySystem = new EnergySystemProxy("guest");

        controller.register("lighting", lightingSystem);
        controller.register("transport", transportSystem);
        controller.register("energy", currentEnergySystem);

        // Smart ko'cha namuna (builder)
        SmartStreet mainStreet = new SmartStreetBuilder()
                .addLamps(35)
                .addCameras(12)
                .addSolarPanels(20)
                .build();

        // Misol uchun Abstract Factory ishlatish:
        DeviceFactory lf = DeviceFactoryProvider.getFactory("lighting");
        DeviceFactory tf = DeviceFactoryProvider.getFactory("transport");
        Device d1 = lf.createDevice();
        Device d2 = tf.createDevice();

        System.out.println("6 ta Design Pattern ishlatildi:");
        System.out.println("• Singleton      • Facade");
        System.out.println("• Abstract Factory + Factory Method");
        System.out.println("• Builder        • Decorator");
        System.out.println("• Proxy\n");

        // Konsol menyusi
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            showMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    controller.turnOnAllLights();
                    break;
                case "2":
                    controller.emergencyStopTraffic();
                    break;
                case "3":
                    System.out.println(controller.getEnergyReport());
                    break;
                case "4":
                    // admin rejimga o'tish
                    currentEnergySystem = new EnergySystemProxy("admin");
                    controller.register("energy", currentEnergySystem);
                    System.out.println("ADMIN rejimi yoqildi! Endi energiya sozlamalarini o'zgartirishingiz mumkin");
                    break;
                case "5":
                    System.out.println(mainStreet.info());
                    break;
                case "6":
                    System.out.print("Yangi rejim (eco / max): ");
                    String mode = scanner.nextLine().trim();
                    controller.setEnergyMode(mode);
                    break;
                case "0":
                    System.out.println("\nSmartCity o'chirildi. Xayr!");
                    running = false;
                    break;
                default:
                    System.out.println("Noto'g'ri tanlov. Qaytadan urinib ko'ring.");
            }

            // kichik pauza (optional)
            try { TimeUnit.MILLISECONDS.sleep(600); } catch (InterruptedException ignored) {}
        }

        scanner.close();
    }

    private static void showMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("     SMART CITY BOSHQARUV PANELI");
        System.out.println("=".repeat(40));
        System.out.println("1. Barcha chiroqlarni yoqish");
        System.out.println("2. Favqulodda transportni to'xtatish");
        System.out.println("3. Energiya hisoboti");
        System.out.println("4. Admin rejimga o‘tish");
        System.out.println("5. Smart ko‘cha ma'lumotlari");
        System.out.println("6. Energiya rejimini o‘zgartirish (eco / max)");
        System.out.println("0. Chiqish");
        System.out.println("-".repeat(40));
        System.out.print("Tanlovingiz: ");
    }
}
