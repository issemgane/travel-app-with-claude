package com.wanderlust.api.config;

import com.wanderlust.api.interaction.Comment;
import com.wanderlust.api.interaction.CommentRepository;
import com.wanderlust.api.interaction.Like;
import com.wanderlust.api.interaction.LikeRepository;
import com.wanderlust.api.post.*;
import com.wanderlust.api.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TravelPostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already has data, skipping seed.");
            return;
        }

        log.info("Seeding database with sample data...");

        String hashedPassword = passwordEncoder.encode("password123");

        // Create users
        List<User> users = new ArrayList<>();
        Object[][] userData = {
            {"alexwanderer", "Alex Thompson", "alex@wanderlust.com", TravelStyle.BACKPACKER, 27, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"},
            {"sophiatravel", "Sophia Chen", "sophia@wanderlust.com", TravelStyle.SOLO, 34, "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150"},
            {"marcoadventure", "Marco Rossi", "marco@wanderlust.com", TravelStyle.ADVENTURE, 22, "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150"},
            {"emilynomad", "Emily Park", "emily@wanderlust.com", TravelStyle.DIGITAL_NOMAD, 41, "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150"},
            {"jakefamily", "Jake Williams", "jake@wanderlust.com", TravelStyle.FAMILY, 15, "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150"},
            {"lunaluxury", "Luna Martinez", "luna@wanderlust.com", TravelStyle.LUXURY, 52, "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150"},
            {"nomadryan", "Ryan Nakamura", "ryan@wanderlust.com", TravelStyle.DIGITAL_NOMAD, 19, "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150"},
            {"wanderingmia", "Mia Johnson", "mia@wanderlust.com", TravelStyle.BACKPACKER, 31, "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150"},
        };

        for (Object[] u : userData) {
            User user = User.builder()
                    .username((String) u[0])
                    .displayName((String) u[1])
                    .email((String) u[2])
                    .passwordHash(hashedPassword)
                    .bio("Travel enthusiast exploring the world one destination at a time.")
                    .travelStyle((TravelStyle) u[3])
                    .countriesVisitedCount((Integer) u[4])
                    .avatarUrl((String) u[5])
                    .build();
            users.add(userRepository.save(user));
        }

        // Create follows (everyone follows a few others)
        Random rng = new Random(42);
        for (User follower : users) {
            List<User> others = new ArrayList<>(users);
            others.remove(follower);
            Collections.shuffle(others, rng);
            int count = 3 + rng.nextInt(4);
            for (int i = 0; i < Math.min(count, others.size()); i++) {
                Follow follow = Follow.builder()
                        .id(new FollowId(follower.getId(), others.get(i).getId()))
                        .build();
                followRepository.save(follow);
            }
        }

        // Create posts
        List<TravelPost> posts = new ArrayList<>();
        Object[][] postData = {
            {0, "Just arrived at Santorini and the sunset views from Oia are absolutely breathtaking! The blue domes against the golden sky is something everyone needs to experience at least once. Got a great spot at a cliff-side restaurant.", PostCategory.SPOT, (short)4, "SUMMER", "5-7 days", 36.4618, 25.3753, "Santorini, Oia", "GR", "sunset,greece,islands", "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?w=800"},
            {0, "Pro tip: Visit the temples in Bali early morning (before 7am). You'll have the place almost to yourself and the light is perfect for photos. Avoid weekends if possible!", PostCategory.TIP, (short)2, "SPRING", "10-14 days", -8.3405, 115.0920, "Ubud, Bali", "ID", "bali,temples,tips", "https://images.unsplash.com/photo-1537996194471-e657df975ab4?w=800"},
            {1, "The street food in Bangkok's Chinatown (Yaowarat) is unreal. Pad thai for $1, mango sticky rice that melts in your mouth, and the freshest seafood you'll ever taste. Come hungry!", PostCategory.FOOD, (short)1, "WINTER", "3-5 days", 13.7399, 100.5100, "Yaowarat Road, Bangkok", "TH", "bangkok,streetfood,thai", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800"},
            {1, "Hiking the Inca Trail to Machu Picchu was the most challenging and rewarding thing I've ever done. 4 days through cloud forests and ancient ruins. Book at least 6 months in advance!", PostCategory.ACTIVITY, (short)3, "SPRING", "4 days", -13.1631, -72.5450, "Machu Picchu", "PE", "peru,hiking,incatrail", "https://images.unsplash.com/photo-1587595431973-160d0d94add1?w=800"},
            {2, "Found this incredible ryokan in Kyoto with a private onsen. Traditional tatami rooms, kaiseki dinner included. Worth every penny for the cultural experience.", PostCategory.STAY, (short)4, "AUTUMN", "3-4 days", 35.0116, 135.7681, "Kyoto", "JP", "japan,kyoto,ryokan", "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800"},
            {2, "Warning: The taxis from Marrakech airport will try to charge you 10x the normal fare. Use the official taxi stand or arrange pickup through your riad. Normal price is around 70-100 MAD.", PostCategory.WARNING, (short)2, "SPRING", "4-5 days", 31.6295, -7.9811, "Marrakech", "MA", "morocco,scam,warning", "https://images.unsplash.com/photo-1489749798305-4fea3ae63d43?w=800"},
            {3, "Working from this co-working space in Lisbon with ocean views. Fast wifi, great coffee, and the digital nomad community here is amazing. Rent is super affordable compared to other EU capitals.", PostCategory.SPOT, (short)2, "SPRING", "1-3 months", 38.7223, -9.1393, "Lisbon", "PT", "digitalnomad,lisbon,coworking", "https://images.unsplash.com/photo-1585208798174-6cedd86e019a?w=800"},
            {3, "The Northern Lights in Tromso exceeded all expectations. We booked a guided chase tour and they drove us 2 hours outside the city to clear skies. Absolutely magical experience.", PostCategory.ACTIVITY, (short)4, "WINTER", "3-4 days", 69.6492, 18.9553, "Tromso", "NO", "northernlights,norway,aurora", "https://images.unsplash.com/photo-1483347756197-71ef80e95f73?w=800"},
            {4, "Traveling with kids in Tokyo is surprisingly easy. The trains are family-friendly, there are parks everywhere, and TeamLab Borderless blew their minds. Best family trip ever!", PostCategory.TIP, (short)4, "SPRING", "7-10 days", 35.6762, 139.6503, "Tokyo", "JP", "tokyo,family,kids", "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800"},
            {4, "This all-inclusive resort in Cancun has an amazing kids club. Parents can actually relax while the little ones are entertained. The beach is pristine and calm enough for toddlers.", PostCategory.STAY, (short)4, "WINTER", "5-7 days", 21.1619, -86.8515, "Cancun", "MX", "cancun,family,resort", "https://images.unsplash.com/photo-1510414842594-a61c69b5ae57?w=800"},
            {5, "The overwater bungalows in Bora Bora are as dreamy as they look. Woke up to fish swimming under my glass floor. The Four Seasons here is next level luxury.", PostCategory.STAY, (short)5, "SUMMER", "5-7 days", -16.5004, -151.7415, "Bora Bora", "PF", "borabora,luxury,overwater", "https://images.unsplash.com/photo-1590523741831-ab7e8b8f9c7f?w=800"},
            {5, "Dinner at a Michelin star restaurant in Paris with views of the Eiffel Tower sparkling at night. The tasting menu was 9 courses of pure art. A once-in-a-lifetime dining experience.", PostCategory.FOOD, (short)5, "AUTUMN", "4-5 days", 48.8566, 2.3522, "Paris", "FR", "paris,michelin,finedining", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800"},
            {6, "Chiang Mai is the perfect base for remote workers. Great cafes, cheap accommodation, amazing food, and you can explore temples and night markets after work. $800/month all-in!", PostCategory.TIP, (short)1, "WINTER", "1-3 months", 18.7883, 98.9853, "Chiang Mai", "TH", "chiangmai,nomad,budget", "https://images.unsplash.com/photo-1528181304800-259b08848526?w=800"},
            {6, "Scuba diving in the Great Barrier Reef was incredible. The coral is even more colorful than photos show. We saw sea turtles, reef sharks, and countless tropical fish.", PostCategory.ACTIVITY, (short)3, "SPRING", "3-5 days", -16.5004, 145.7781, "Great Barrier Reef", "AU", "diving,australia,reef", "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800"},
            {7, "Backpacking through the Scottish Highlands on the West Highland Way. 96 miles of the most dramatic landscapes I've ever seen. Wild camping is legal here!", PostCategory.ACTIVITY, (short)1, "SUMMER", "7-8 days", 56.8198, -5.1052, "West Highland Way", "GB", "scotland,hiking,backpacking", "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800"},
            {7, "The ramen in Osaka's Dotonbori district is life-changing. Ichiran's tonkotsu broth is rich and creamy. The whole area comes alive at night with neon signs and food stalls.", PostCategory.FOOD, (short)1, "AUTUMN", "3-4 days", 34.6687, 135.5013, "Osaka, Dotonbori", "JP", "osaka,ramen,japan", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800"},
            {0, "Cappadocia's hot air balloons at sunrise is pure magic. Over 100 balloons rising over fairy chimneys and cave dwellings. Book the earliest flight for the best light.", PostCategory.ACTIVITY, (short)3, "SPRING", "3-4 days", 38.6431, 34.8287, "Cappadocia", "TR", "turkey,balloons,sunrise", "https://images.unsplash.com/photo-1641128324972-af3212f0f6bd?w=800"},
            {1, "The cenotes near Tulum are nature's swimming pools. Crystal clear water in underground caves. Cenote Suytun has a single beam of light that hits the water. Unreal.", PostCategory.SPOT, (short)2, "WINTER", "3-5 days", 20.2114, -87.4654, "Tulum Cenotes", "MX", "mexico,cenotes,swimming", "https://images.unsplash.com/photo-1552074284-5e88ef1aef18?w=800"},
            {2, "Exploring the medina in Fez feels like stepping back in time. The world's oldest university, leather tanneries, and getting lost in 9,000+ alleyways. Hire a guide for the first day!", PostCategory.SPOT, (short)2, "SPRING", "2-3 days", 34.0181, -5.0078, "Fez Medina", "MA", "morocco,fez,culture", "https://images.unsplash.com/photo-1548018560-c7196e6c2c26?w=800"},
            {3, "Remote working from a beachfront villa in Canggu, Bali. Fiber internet, pool, ocean view - all for $30/night. This is why I became a digital nomad.", PostCategory.STAY, (short)2, "SUMMER", "1-3 months", -8.6478, 115.1385, "Canggu, Bali", "ID", "bali,nomad,villa", "https://images.unsplash.com/photo-1559628233-100c798642d4?w=800"},
            {4, "The Swiss Alps with kids: Grindelwald has an adventure playground with zip lines and the views of the Eiger are jaw-dropping. Kids loved the mountain train ride too.", PostCategory.ACTIVITY, (short)4, "SUMMER", "5-7 days", 46.6244, 8.0413, "Grindelwald", "CH", "switzerland,alps,family", "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?w=800"},
            {5, "Safari in the Serengeti during the Great Migration. Watched a million wildebeest cross the Mara River. Our luxury tented camp had butlers and gourmet bush dinners.", PostCategory.ACTIVITY, (short)5, "SUMMER", "5-7 days", -2.3333, 34.8333, "Serengeti", "TZ", "safari,serengeti,wildlife", "https://images.unsplash.com/photo-1516426122078-c23e76b4f7b6?w=800"},
            {6, "Medellin's transformation is incredible. The cable cars, street art in Comuna 13, perfect weather year-round. Coffee tours in nearby farms are a must. Best city in Colombia.", PostCategory.SPOT, (short)1, "SPRING", "5-7 days", 6.2442, -75.5812, "Medellin", "CO", "colombia,medellin,culture", "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=800"},
            {7, "The Blue Lagoon in Iceland is touristy but worth it. Book the Retreat Spa for a more exclusive experience. The contrast of hot water and cold air with snow falling is magical.", PostCategory.SPOT, (short)4, "WINTER", "4-5 days", 63.8804, -22.4495, "Blue Lagoon, Iceland", "IS", "iceland,bluelagoon,spa", "https://images.unsplash.com/photo-1515488764276-beab7607c1e6?w=800"},
            {0, "Vietnamese pho in Hanoi's Old Quarter at 6am is a spiritual experience. Locals squatting on tiny plastic stools, steaming bowls of perfectly spiced broth. Total cost: $1.50.", PostCategory.FOOD, (short)1, "AUTUMN", "5-7 days", 21.0285, 105.8542, "Hanoi Old Quarter", "VN", "vietnam,pho,streetfood", "https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=800"},
            {1, "Patagonia's Torres del Paine W Trek is challenging but the views of glaciers, turquoise lakes, and granite towers make every step worth it. Go in December-February.", PostCategory.ACTIVITY, (short)3, "SUMMER", "5-7 days", -51.0000, -73.0000, "Torres del Paine", "CL", "patagonia,trekking,chile", "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=800"},
            {3, "The co-living scene in Tbilisi is blowing up. Great Georgian wine, $2 khachapuri, beautiful architecture, and a growing tech scene. Visa-free for most nationalities.", PostCategory.TIP, (short)1, "AUTUMN", "1-3 months", 41.7151, 44.8271, "Tbilisi", "GE", "georgia,nomad,coliving", "https://images.unsplash.com/photo-1565008447742-97f6f38c985c?w=800"},
            {5, "Amalfi Coast by private yacht. Stopped at Positano, Ravello, and the hidden beaches you can only access by boat. The limoncello spritz at sunset was perfection.", PostCategory.ACTIVITY, (short)5, "SUMMER", "5-7 days", 40.6340, 14.6027, "Amalfi Coast", "IT", "italy,amalfi,yacht", "https://images.unsplash.com/photo-1534008897995-27a23e859048?w=800"},
            {2, "Camping under the stars in the Sahara Desert. Rode camels to our desert camp, had traditional Berber dinner, then watched the Milky Way stretch across the sky.", PostCategory.ACTIVITY, (short)2, "AUTUMN", "2-3 days", 31.0000, -4.0000, "Sahara Desert", "MA", "sahara,camping,stars", "https://images.unsplash.com/photo-1509023464722-18d996393ca8?w=800"},
            {4, "Dubrovnik Old Town is like walking through a Game of Thrones set (because it literally is). The city walls walk gives incredible views. Go early to beat cruise ship crowds.", PostCategory.SPOT, (short)3, "SPRING", "2-3 days", 42.6507, 18.0944, "Dubrovnik", "HR", "croatia,dubrovnik,got", "https://images.unsplash.com/photo-1555990793-da11153b2473?w=800"},
        };

        for (Object[] p : postData) {
            TravelPost post = TravelPost.builder()
                    .user(users[(int) p[0]])
                    .content((String) p[1])
                    .category((PostCategory) p[2])
                    .costLevel((Short) p[3])
                    .bestSeason((String) p[4])
                    .durationSuggested((String) p[5])
                    .latitude((Double) p[6])
                    .longitude((Double) p[7])
                    .placeName((String) p[8])
                    .countryCode((String) p[9])
                    .tags((String) p[10])
                    .likesCount(5 + rng.nextInt(95))
                    .commentsCount(0)
                    .build();

            PostMedia media = PostMedia.builder()
                    .post(post)
                    .mediaUrl((String) p[11])
                    .mediaType(MediaType.IMAGE)
                    .displayOrder(0)
                    .width(800)
                    .height(600)
                    .build();
            post.getMedia().add(media);

            posts.add(postRepository.save(post));
        }

        // Add comments
        String[][] comments = {
            {"This is absolutely stunning! Adding to my bucket list.", "How long did you stay here?"},
            {"Incredible photo! What camera did you use?", "I was here last year, it's even better in person!"},
            {"Thanks for the tip, super helpful!", "Definitely bookmarking this for my next trip."},
            {"Wow, this looks amazing! How much did the whole trip cost?", "I need to go here ASAP."},
            {"Best travel advice I've seen on here.", "Can you recommend any specific hotels nearby?"},
            {"This makes me want to quit my job and travel!", "What was the highlight of your trip?"},
        };

        for (int i = 0; i < posts.size(); i++) {
            TravelPost post = posts.get(i);
            int numComments = 2 + rng.nextInt(4);
            for (int j = 0; j < numComments; j++) {
                User commenter = users[rng.nextInt(users.size())];
                String[] pair = comments[rng.nextInt(comments.length)];
                Comment comment = Comment.builder()
                        .postId(post.getId())
                        .user(commenter)
                        .content(pair[rng.nextInt(pair.length)])
                        .isQuestion(rng.nextBoolean())
                        .isAnswer(false)
                        .build();
                commentRepository.save(comment);
            }
            post.setCommentsCount(numComments);
            postRepository.save(post);
        }

        // Add likes
        for (TravelPost post : posts) {
            Set<UUID> likedBy = new HashSet<>();
            int numLikes = post.getLikesCount();
            List<User> shuffledUsers = new ArrayList<>(users);
            Collections.shuffle(shuffledUsers, rng);
            for (int j = 0; j < Math.min(numLikes, shuffledUsers.size()); j++) {
                User liker = shuffledUsers.get(j);
                if (likedBy.add(liker.getId())) {
                    Like like = Like.builder()
                            .userId(liker.getId())
                            .postId(post.getId())
                            .build();
                    likeRepository.save(like);
                }
            }
            post.setLikesCount(likedBy.size());
            postRepository.save(post);
        }

        log.info("Seeded {} users, {} posts with comments and likes.", users.size(), posts.size());
    }
}
