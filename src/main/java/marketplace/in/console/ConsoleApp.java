package marketplace.in.console;

import marketplace.application.AuditService;
import marketplace.application.AuthService;
import marketplace.application.ProductService;
import marketplace.domain.Product;
import marketplace.domain.User;
import marketplace.out.file.FileAuditRepository;
import marketplace.out.cache.LruCache;
import marketplace.out.file.FileMetaRepository;
import marketplace.out.file.FileProductRepository;
import marketplace.out.file.FileUserRepository;


import java.nio.file.Path;
import java.util.*;

/**
 * Консольное приложение — меню взаимодействия с пользователем.
 */

public class ConsoleApp {
    public static void main(String[] args) throws Exception {
        Path dataDir = Path.of(".");
        FileProductRepository repo = new FileProductRepository(dataDir.resolve("products.db"));
        LruCache<String, List<Product>> cache = new LruCache<>(50);
        ProductService service = new ProductService(repo, cache);

        FileUserRepository userRepo = new FileUserRepository(dataDir.resolve("users.db"));
        AuthService authService = new AuthService(userRepo);

        FileMetaRepository meta = new FileMetaRepository(dataDir.resolve("meta.db"));

        FileAuditRepository auditRepo = new FileAuditRepository(dataDir.resolve("audit.log"));
        AuditService audit = new AuditService(auditRepo);

        Scanner scanner = new Scanner(System.in);
        User current = null;

        mainLoop:
        while (true) {
            if (current == null) {
                System.out.println("=== Product Catalog Service ===");
                System.out.println("1) Войти");
                System.out.println("2) Зарегистрироваться");
                System.out.println("3) Выйти");
                System.out.print("> ");
                String opt = scanner.nextLine().trim();
                switch (opt) {
                    case "1":
                        current = handleLogin(scanner, authService, audit);
                        break;
                    case "2":
                        handleRegister(scanner, authService, audit);
                        break;
                    case "3":
                        System.out.println("Работа завершера");
                        break mainLoop;
                    default:
                        System.out.println("Неизвестная операция");
                }
            } else {
                System.out.printf("User: %s (%s)\n", current.getUsername(), current.getRole());
                System.out.println("1) Добавить продукт");
                System.out.println("2) Обновить продукт");
                System.out.println("3) Удалить продукт");
                System.out.println("4) Найти продукт по номеру (id)");
                System.out.println("5) Поиск продуктов");
                System.out.println("6) Всего продуктов");
                System.out.println("7) Управление категориями/брендами");
                System.out.println("8) Выход из системы");
                System.out.println("9) Завершение работы");
                System.out.print("> ");
                String opt = scanner.nextLine().trim();
                switch (opt) {
                    case "1":
                        handleAddProduct(scanner, service, meta, audit, current);
                        break;
                    case "2":
                        handleUpdateProduct(scanner, service, meta, audit, current);
                        break;
                    case "3":
                        handleDeleteProduct(scanner, service, audit, current);
                        break;
                    case "4":
                        handleViewProduct(scanner, service);
                        break;
                    case "5":
                        handleSearch(scanner, service, audit, current);
                        break;
                    case "6":
                        System.out.println("Всего продуктов =" + service.count());
                        break;
                    case "7":
                        manageMeta(scanner, meta);
                        break;
                    case "8":
                        audit.record(current, "logout", "user logged out");
                        current = null;
                        break;
                    case "9":
                        audit.record(current, "exit", "application exit");
                        System.out.println("Работа завершера");
                        break mainLoop;
                    default:
                        System.out.println("Неизвестная операция");
                }
            }
        }
    }

    private static User handleLogin(Scanner scanner, AuthService authService, AuditService audit) {
        System.out.print("Имя пользователя (username): ");
        String u = scanner.nextLine().trim();
        System.out.print("Пароль (password): ");
        String p = scanner.nextLine().trim();
        Optional<User> maybe = authService.authenticate(u, p);
        if (maybe.isPresent()) {
            User user = maybe.get();
            audit.record(user, "login", "successful login");
            System.out.println("Пользователь " + user.getUsername() + " вошел в систему");
            return user;
        } else {
            audit.record(null, "login_failed", "username=" + u);
            System.out.println("Неверные учетные данные");
            return null;
        }
    }

    private static void handleRegister(Scanner scanner, AuthService authService, AuditService audit) {
        System.out.print("Имя пользователя (username): ");
        String u = scanner.nextLine().trim();
        System.out.print("Пароль (password): ");
        String p = scanner.nextLine().trim();
        System.out.print("role (ADMIN/USER): ");
        String r = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
        if (!r.equals("ADMIN") && !r.equals("USER")) r = "USER";
        boolean ok = authService.register(u, p, r);
        if (ok) {
            audit.record(null, "register", "username=" + u);
            System.out.println("Зарегистрирован");
        } else {
            System.out.println("Пользователь с таким именем уже существует");
        }
    }

    private static void handleAddProduct(Scanner scanner, ProductService service, FileMetaRepository meta, AuditService audit, User current) {
        System.out.print("Добавить номер (id): "); String id = scanner.nextLine().trim();
        System.out.print("Название продукта: "); String name = scanner.nextLine().trim();

        String category = chooseOrCreate(scanner, "категорию", meta.listCategories(), c -> meta.addCategory(c));
        String brand = chooseOrCreate(scanner, "бренд", meta.listBrands(), b -> meta.addBrand(b));

        System.out.print("Цена: "); double price = Double.parseDouble(scanner.nextLine().trim());
        Product p = new Product(id, name, category, brand, price);
        service.addProduct(p);
        audit.record(current, "add_product", p.getId());
        System.out.println("Продукт добавлен");
    }

    private static void handleUpdateProduct(Scanner scanner, ProductService service, FileMetaRepository meta, AuditService audit, User current) {
        System.out.print("Введите номер продукта (id): "); String id = scanner.nextLine().trim();
        Optional<Product> opt = service.getById(id);
        if (opt.isEmpty()) { System.out.println("Не найден"); return; }
        Product ex = opt.get();
        System.out.print("Название ("+ex.getName()+"): "); String newName = scanner.nextLine().trim(); if (!newName.isEmpty()) ex.setName(newName);
        System.out.println("Выберите категорию:");
        String newCat = chooseOrCreate(scanner, "категорию", meta.listCategories(), c -> meta.addCategory(c)); ex.setCategory(newCat);
        System.out.println("Выберите бренд:");
        String newBrand = chooseOrCreate(scanner, "бренд", meta.listBrands(), b -> meta.addBrand(b)); ex.setBrand(newBrand);
        System.out.print("Цена ("+ex.getPrice()+"): "); String p = scanner.nextLine().trim(); if (!p.isEmpty()) ex.setPrice(Double.parseDouble(p));
        boolean ok = service.updateProduct(id, ex);
        audit.record(current, "update_product", id);
        System.out.println(ok ? "Обновлено" : "Произошла ошибка");
    }

    private static void handleDeleteProduct(Scanner scanner, ProductService service, AuditService audit, User current) {
        System.out.print("Введите номер продукта (id): "); String id = scanner.nextLine().trim();
        boolean ok = service.deleteProduct(id);
        audit.record(current, "delete_product", id);
        System.out.println(ok ? "Удалено" : "Не найдено");
    }

    private static void handleViewProduct(Scanner scanner, ProductService service) {
        System.out.print("Введите номер продукта (id): "); String id = scanner.nextLine().trim();
        service.getById(id).ifPresentOrElse(System.out::println, () -> System.out.println("Не найдено"));
    }

    private static void handleSearch(Scanner scanner, ProductService service, AuditService audit, User current) {
        Map<String, String> criteria = new HashMap<>();
        System.out.print("Категория (не обязательно): "); String sCat = scanner.nextLine().trim(); if (!sCat.isEmpty()) criteria.put("category", sCat);
        System.out.print("Бренд (не обязательно): "); String sBrand = scanner.nextLine().trim(); if (!sBrand.isEmpty()) criteria.put("brand", sBrand);
        System.out.print("Название (не обязательно): "); String sName = scanner.nextLine().trim(); if (!sName.isEmpty()) criteria.put("name", sName);
        System.out.print("Минимальная цена (не обязательно): "); String sMin = scanner.nextLine().trim(); Double min = sMin.isEmpty() ? null : Double.parseDouble(sMin);
        System.out.print("Максимальная цена (не обязательно): "); String sMax = scanner.nextLine().trim(); Double max = sMax.isEmpty() ? null : Double.parseDouble(sMax);
        List<Product> found = service.search(criteria, min, max);
        found.forEach(System.out::println);
        audit.record(current, "search", String.format("criteria=%s,min=%s,max=%s", criteria, min, max));
    }

    private static void manageMeta(Scanner scanner, FileMetaRepository meta) {
        while (true) {
            System.out.println("1) Список категорий");
            System.out.println("2) Добавить категорию");
            System.out.println("3) Удалить категорию");
            System.out.println("4) Список брендов");
            System.out.println("5) Добавить бренд");
            System.out.println("6) Удалить бренд");
            System.out.println("7) Назад");
            System.out.print("> ");
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1": meta.listCategories().forEach(System.out::println); break;
                case "2": System.out.print("категория: "); meta.addCategory(scanner.nextLine().trim()); break;
                case "3": System.out.print("категория: "); meta.removeCategory(scanner.nextLine().trim()); break;
                case "4": meta.listBrands().forEach(System.out::println); break;
                case "5": System.out.print("бренд: "); meta.addBrand(scanner.nextLine().trim()); break;
                case "6": System.out.print("бренд: "); meta.removeBrand(scanner.nextLine().trim()); break;
                case "7": return;
                default: System.out.println("Неизвестная операция");
            }
        }
    }

    private static String chooseOrCreate(Scanner scanner, String name, List<String> options, java.util.function.Consumer<String> onCreate) {
        while (true) {
            System.out.println("0) Создать " + name);
            for (int i = 0; i < options.size(); i++) {
                System.out.printf("%d) %s", i+1, options.get(i));
            }
            System.out.print("> ");
            String sel = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(sel);
                if (idx == 0) {
                    System.out.print("Введите " + name + ": ");
                    String v = scanner.nextLine().trim();
                    onCreate.accept(v);
                    return v;
                }
                if (idx >= 1 && idx <= options.size()) return options.get(idx-1);
            } catch (NumberFormatException ex) {

            }
            System.out.println("Неправильный выбор");
        }
    }

}
