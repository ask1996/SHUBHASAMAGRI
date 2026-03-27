package com.shubhasamagri.config;

import com.shubhasamagri.entity.*;
import com.shubhasamagri.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

/**
 * Loads sample data on application startup (development mode).
 * Only runs if database is empty to avoid duplicate inserts on restart.
 *
 * Sample Data:
 *   - 1 Admin user + 1 Test user
 *   - 5 Occasions (Marriage, Gruha Pravesh, Satyanarayana Vratham, Naming Ceremony, Upanayanam)
 *   - 15 Pooja Items (flowers, diyas, incense, etc.)
 *   - 5 Pooja Kits (one per occasion)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OccasionRepository occasionRepository;
    private final PoojaItemRepository poojaItemRepository;
    private final PoojaKitRepository poojaKitRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already has data. Skipping initialization.");
            return;
        }

        log.info("Initializing sample data for ShubhaSamagri...");

        createUsers();
        List<Occasion> occasions = createOccasions();
        List<PoojaItem> items = createPoojaItems();
        createPoojaKits(occasions, items);

        log.info("Sample data initialization complete!");
    }

    private void createUsers() {
        User admin = User.builder()
                .name("Admin")
                .email("admin@shubhasamagri.com")
                .password(passwordEncoder.encode("admin123"))
                .phone("9999999999")
                .role(User.Role.ADMIN)
                .build();

        User testUser = User.builder()
                .name("Ravi Kumar")
                .email("ravi@example.com")
                .password(passwordEncoder.encode("password123"))
                .phone("9876543210")
                .role(User.Role.USER)
                .build();

        userRepository.saveAll(Arrays.asList(admin, testUser));
        log.info("Created users: admin@shubhasamagri.com / admin123 | ravi@example.com / password123");
    }

    private List<Occasion> createOccasions() {
        List<Occasion> occasions = Arrays.asList(
            Occasion.builder()
                .name("Marriage")
                .description("A sacred Hindu wedding ceremony bringing two souls together with Vedic rituals, mangalsutra, and saptapadi (seven vows).")
                .imageUrl("https://images.unsplash.com/photo-1583073733573-f1a02be19ad3?w=400")
                .isActive(true).build(),

            Occasion.builder()
                .name("Gruha Pravesh")
                .description("Housewarming ceremony to purify and sanctify a new home, inviting positive energies and blessings of Lord Ganesha and Vastu Purusha.")
                .imageUrl("https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400")
                .isActive(true).build(),

            Occasion.builder()
                .name("Satyanarayana Vratham")
                .description("A popular puja dedicated to Lord Satyanarayana (Vishnu) performed on full moon days, Ekadashi, or during auspicious events.")
                .imageUrl("https://images.unsplash.com/photo-1531804055935-76f44d7c3621?w=400")
                .isActive(true).build(),

            Occasion.builder()
                .name("Naming Ceremony")
                .description("Namakarana - the sacred ritual performed on the 11th day after a child's birth to give the baby a meaningful name and seek blessings.")
                .imageUrl("https://images.unsplash.com/photo-1544776193-352d25ca82cd?w=400")
                .isActive(true).build(),

            Occasion.builder()
                .name("Upanayanam")
                .description("The sacred thread ceremony (Janeu) marking a boy's initiation into Vedic studies. A significant rite of passage in Hindu tradition.")
                .imageUrl("https://images.unsplash.com/photo-1478476868527-002ae3f3e159?w=400")
                .isActive(true).build()
        );

        return occasionRepository.saveAll(occasions);
    }

    private List<PoojaItem> createPoojaItems() {
        List<PoojaItem> items = Arrays.asList(
            PoojaItem.builder().name("Agarbatti (Incense Sticks)").description("Premium sandalwood and jasmine incense sticks").unit("pack of 20").isAvailable(true).build(),
            PoojaItem.builder().name("Coconut").description("Fresh coconut for pooja offering").unit("pcs").isAvailable(true).build(),
            PoojaItem.builder().name("Turmeric Powder (Haldi)").description("Pure organic turmeric powder").unit("100 grams").isAvailable(true).build(),
            PoojaItem.builder().name("Kumkum (Sindoor)").description("Auspicious red kumkum powder").unit("50 grams").isAvailable(true).build(),
            PoojaItem.builder().name("Camphor (Kapur)").description("Pure white camphor tablets for aarti").unit("pack of 10 tablets").isAvailable(true).build(),
            PoojaItem.builder().name("Ghee").description("Pure cow ghee for havan and offerings").unit("250 ml").isAvailable(true).build(),
            PoojaItem.builder().name("Flowers - Marigold Garland").description("Fresh marigold garland for decoration").unit("pcs").isAvailable(true).build(),
            PoojaItem.builder().name("Banana Leaves").description("Fresh large banana leaves for serving prasad").unit("pack of 5").isAvailable(true).build(),
            PoojaItem.builder().name("Copper Kalash (Pot)").description("Pure copper water pot with lid for sacred water").unit("pcs").isAvailable(true).build(),
            PoojaItem.builder().name("Diya (Oil Lamp)").description("Traditional clay diyas for lighting").unit("pack of 10").isAvailable(true).build(),
            PoojaItem.builder().name("Cotton Wicks").description("Pure cotton wicks for diyas").unit("pack of 50").isAvailable(true).build(),
            PoojaItem.builder().name("Panchagavya").description("Sacred mixture of five cow products - milk, curd, ghee, urine, dung (purified)").unit("packet").isAvailable(true).build(),
            PoojaItem.builder().name("Sesame Seeds (Til)").description("Black sesame seeds for havan and offerings").unit("100 grams").isAvailable(true).build(),
            PoojaItem.builder().name("Betel Leaves (Paan Patta)").description("Fresh betel leaves for tamboolam").unit("pack of 20").isAvailable(true).build(),
            PoojaItem.builder().name("Sacred Thread (Kalava/Mauli)").description("Red and yellow sacred thread for tying").unit("roll").isAvailable(true).build()
        );

        return poojaItemRepository.saveAll(items);
    }

    private void createPoojaKits(List<Occasion> occasions, List<PoojaItem> items) {
        // Map items by name for easy lookup
        Map<String, PoojaItem> itemMap = new HashMap<>();
        items.forEach(item -> itemMap.put(item.getName(), item));

        // Kit 1: Marriage Kit
        createKit(occasions.get(0), "Complete Marriage Pooja Kit",
            "Everything you need for a traditional Hindu wedding ceremony, curated by Vedic pandits. " +
            "Includes all essential items for Ganesh puja, Var mala, Saptapadi and Mangal phere.",
            new BigDecimal("2499.00"), 5,
            Arrays.asList(
                kitItem(itemMap.get("Agarbatti (Incense Sticks)"), 5, null),
                kitItem(itemMap.get("Coconut"), 11, null),
                kitItem(itemMap.get("Turmeric Powder (Haldi)"), 3, "200 grams each"),
                kitItem(itemMap.get("Kumkum (Sindoor)"), 2, null),
                kitItem(itemMap.get("Camphor (Kapur)"), 3, null),
                kitItem(itemMap.get("Ghee"), 2, null),
                kitItem(itemMap.get("Flowers - Marigold Garland"), 10, null),
                kitItem(itemMap.get("Copper Kalash (Pot)"), 2, null),
                kitItem(itemMap.get("Diya (Oil Lamp)"), 2, null),
                kitItem(itemMap.get("Betel Leaves (Paan Patta)"), 3, null),
                kitItem(itemMap.get("Sacred Thread (Kalava/Mauli)"), 2, null)
            ));

        // Kit 2: Gruha Pravesh Kit
        createKit(occasions.get(1), "Gruha Pravesh Starter Kit",
            "Bless your new home with this comprehensive housewarming kit. " +
            "Includes items for Vastu puja, Ganesha puja, and Griha Shanti.",
            new BigDecimal("1299.00"), 3,
            Arrays.asList(
                kitItem(itemMap.get("Agarbatti (Incense Sticks)"), 3, null),
                kitItem(itemMap.get("Coconut"), 3, null),
                kitItem(itemMap.get("Turmeric Powder (Haldi)"), 1, null),
                kitItem(itemMap.get("Kumkum (Sindoor)"), 1, null),
                kitItem(itemMap.get("Camphor (Kapur)"), 2, null),
                kitItem(itemMap.get("Ghee"), 1, null),
                kitItem(itemMap.get("Flowers - Marigold Garland"), 5, null),
                kitItem(itemMap.get("Copper Kalash (Pot)"), 1, null),
                kitItem(itemMap.get("Diya (Oil Lamp)"), 1, null),
                kitItem(itemMap.get("Sacred Thread (Kalava/Mauli)"), 1, null)
            ));

        // Kit 3: Satyanarayana Vratham Kit
        createKit(occasions.get(2), "Satyanarayana Puja Kit",
            "Complete items for performing Shri Satyanarayana Vrat Katha puja as prescribed by the Skanda Purana.",
            new BigDecimal("899.00"), 2,
            Arrays.asList(
                kitItem(itemMap.get("Agarbatti (Incense Sticks)"), 2, null),
                kitItem(itemMap.get("Coconut"), 5, null),
                kitItem(itemMap.get("Turmeric Powder (Haldi)"), 1, null),
                kitItem(itemMap.get("Kumkum (Sindoor)"), 1, null),
                kitItem(itemMap.get("Camphor (Kapur)"), 1, null),
                kitItem(itemMap.get("Ghee"), 1, null),
                kitItem(itemMap.get("Banana Leaves"), 2, null),
                kitItem(itemMap.get("Flowers - Marigold Garland"), 3, null),
                kitItem(itemMap.get("Diya (Oil Lamp)"), 1, null),
                kitItem(itemMap.get("Betel Leaves (Paan Patta)"), 1, null)
            ));

        // Kit 4: Naming Ceremony Kit
        createKit(occasions.get(3), "Namakarana (Naming Ceremony) Kit",
            "All essentials for the sacred Namakarana samskara performed on the 11th day after birth.",
            new BigDecimal("699.00"), 2,
            Arrays.asList(
                kitItem(itemMap.get("Agarbatti (Incense Sticks)"), 2, null),
                kitItem(itemMap.get("Coconut"), 2, null),
                kitItem(itemMap.get("Turmeric Powder (Haldi)"), 1, null),
                kitItem(itemMap.get("Kumkum (Sindoor)"), 1, null),
                kitItem(itemMap.get("Camphor (Kapur)"), 1, null),
                kitItem(itemMap.get("Flowers - Marigold Garland"), 2, null),
                kitItem(itemMap.get("Diya (Oil Lamp)"), 1, null),
                kitItem(itemMap.get("Sacred Thread (Kalava/Mauli)"), 1, null),
                kitItem(itemMap.get("Betel Leaves (Paan Patta)"), 1, null)
            ));

        // Kit 5: Upanayanam Kit
        createKit(occasions.get(4), "Upanayanam (Sacred Thread) Kit",
            "Complete Yagnopaveetham ceremony kit including the Janeu and all ritual items " +
            "for this important coming-of-age samskara.",
            new BigDecimal("1599.00"), 4,
            Arrays.asList(
                kitItem(itemMap.get("Agarbatti (Incense Sticks)"), 3, null),
                kitItem(itemMap.get("Coconut"), 7, null),
                kitItem(itemMap.get("Turmeric Powder (Haldi)"), 2, null),
                kitItem(itemMap.get("Kumkum (Sindoor)"), 1, null),
                kitItem(itemMap.get("Camphor (Kapur)"), 2, null),
                kitItem(itemMap.get("Ghee"), 1, null),
                kitItem(itemMap.get("Panchagavya"), 1, null),
                kitItem(itemMap.get("Sesame Seeds (Til)"), 2, null),
                kitItem(itemMap.get("Flowers - Marigold Garland"), 5, null),
                kitItem(itemMap.get("Banana Leaves"), 1, null),
                kitItem(itemMap.get("Diya (Oil Lamp)"), 1, null),
                kitItem(itemMap.get("Sacred Thread (Kalava/Mauli)"), 3, null),
                kitItem(itemMap.get("Betel Leaves (Paan Patta)"), 2, null)
            ));

        log.info("Created 5 pooja kits with all items");
    }

    private void createKit(Occasion occasion, String name, String description,
                            BigDecimal price, int deliveryDays, List<KitItem> kitItemsData) {
        PoojaKit kit = PoojaKit.builder()
                .name(name)
                .description(description)
                .occasion(occasion)
                .price(price)
                .estimatedDeliveryDays(deliveryDays)
                .isActive(true)
                .build();

        kit = poojaKitRepository.save(kit);

        // Assign parent kit reference
        final PoojaKit savedKit = kit;
        kitItemsData.forEach(ki -> ki.setPoojaKit(savedKit));
        kit.setKitItems(kitItemsData);
        poojaKitRepository.save(kit);
    }

    private KitItem kitItem(PoojaItem item, int quantity, String unit) {
        return KitItem.builder()
                .poojaItem(item)
                .quantity(quantity)
                .unit(unit)
                .build();
    }
}
