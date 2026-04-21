package com.wanderlust.api.post;

import com.wanderlust.api.common.ApiException;
import com.wanderlust.api.user.User;
import com.wanderlust.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelPostServiceTest {

    @Mock
    private TravelPostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TravelPostService travelPostService;

    @Captor
    private ArgumentCaptor<TravelPost> postCaptor;

    private User createUser() {
        User user = User.builder()
                .username("traveler42")
                .displayName("Jane Traveler")
                .email("jane@example.com")
                .passwordHash("hashed")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();
        user.setId(UUID.randomUUID());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    private CreatePostRequest createPostRequest() {
        CreatePostRequest request = new CreatePostRequest();
        request.setContent("Amazing place in Tokyo!");
        request.setCategory(PostCategory.SPOT);
        request.setLatitude(35.6762);
        request.setLongitude(139.6503);
        request.setPlaceName("Shibuya Crossing");
        request.setCountryCode("jp");
        request.setCostLevel((short) 3);
        request.setBestSeason("spring");
        request.setDurationSuggested("2 hours");
        request.setAccessibilityRating((short) 4);
        request.setTags(new String[]{"tokyo", "culture"});

        CreatePostRequest.MediaItem mediaItem = new CreatePostRequest.MediaItem();
        mediaItem.setMediaUrl("https://example.com/photo.jpg");
        mediaItem.setMediaType(MediaType.IMAGE);
        mediaItem.setWidth(1920);
        mediaItem.setHeight(1080);
        request.setMediaItems(List.of(mediaItem));

        return request;
    }

    private TravelPost createTravelPost(User user) {
        TravelPost post = TravelPost.builder()
                .user(user)
                .content("Amazing place in Tokyo!")
                .category(PostCategory.SPOT)
                .costLevel((short) 3)
                .bestSeason("spring")
                .durationSuggested("2 hours")
                .accessibilityRating((short) 4)
                .latitude(35.6762)
                .longitude(139.6503)
                .placeName("Shibuya Crossing")
                .countryCode("JP")
                .tags("tokyo,culture")
                .build();
        post.setId(UUID.randomUUID());
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());

        PostMedia media = PostMedia.builder()
                .post(post)
                .mediaUrl("https://example.com/photo.jpg")
                .mediaType(MediaType.IMAGE)
                .displayOrder(0)
                .width(1920)
                .height(1080)
                .build();
        media.setId(UUID.randomUUID());
        media.setCreatedAt(Instant.now());
        media.setUpdatedAt(Instant.now());
        post.getMedia().add(media);

        return post;
    }

    @Test
    void create_savesPostWithCorrectFieldsAndMediaItems() {
        UUID userId = UUID.randomUUID();
        User user = createUser();
        user.setId(userId);
        CreatePostRequest request = createPostRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.save(any(TravelPost.class))).thenAnswer(invocation -> {
            TravelPost saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setCreatedAt(Instant.now());
            saved.setUpdatedAt(Instant.now());
            for (PostMedia m : saved.getMedia()) {
                m.setId(UUID.randomUUID());
                m.setCreatedAt(Instant.now());
                m.setUpdatedAt(Instant.now());
            }
            return saved;
        });

        PostDto result = travelPostService.create(userId, request);

        verify(postRepository).save(postCaptor.capture());
        TravelPost captured = postCaptor.getValue();

        assertThat(captured.getUser()).isEqualTo(user);
        assertThat(captured.getContent()).isEqualTo("Amazing place in Tokyo!");
        assertThat(captured.getCategory()).isEqualTo(PostCategory.SPOT);
        assertThat(captured.getCostLevel()).isEqualTo((short) 3);
        assertThat(captured.getBestSeason()).isEqualTo("spring");
        assertThat(captured.getDurationSuggested()).isEqualTo("2 hours");
        assertThat(captured.getAccessibilityRating()).isEqualTo((short) 4);
        assertThat(captured.getLatitude()).isEqualTo(35.6762);
        assertThat(captured.getLongitude()).isEqualTo(139.6503);
        assertThat(captured.getPlaceName()).isEqualTo("Shibuya Crossing");
        assertThat(captured.getCountryCode()).isEqualTo("JP");
        assertThat(captured.getTags()).isEqualTo("tokyo,culture");
        assertThat(captured.getMedia()).hasSize(1);

        PostMedia capturedMedia = captured.getMedia().get(0);
        assertThat(capturedMedia.getMediaUrl()).isEqualTo("https://example.com/photo.jpg");
        assertThat(capturedMedia.getMediaType()).isEqualTo(MediaType.IMAGE);
        assertThat(capturedMedia.getDisplayOrder()).isZero();
        assertThat(capturedMedia.getWidth()).isEqualTo(1920);
        assertThat(capturedMedia.getHeight()).isEqualTo(1080);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Amazing place in Tokyo!");
    }

    @Test
    void create_throwsNotFoundWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        CreatePostRequest request = createPostRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelPostService.create(userId, request))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(postRepository, never()).save(any());
    }

    @Test
    void create_defaultsCategoryToSpotWhenNull() {
        UUID userId = UUID.randomUUID();
        User user = createUser();
        user.setId(userId);
        CreatePostRequest request = createPostRequest();
        request.setCategory(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.save(any(TravelPost.class))).thenAnswer(invocation -> {
            TravelPost saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setCreatedAt(Instant.now());
            saved.setUpdatedAt(Instant.now());
            for (PostMedia m : saved.getMedia()) {
                m.setId(UUID.randomUUID());
                m.setCreatedAt(Instant.now());
                m.setUpdatedAt(Instant.now());
            }
            return saved;
        });

        travelPostService.create(userId, request);

        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getCategory()).isEqualTo(PostCategory.SPOT);
    }

    @Test
    void getById_returnsPostDtoWhenFound() {
        User user = createUser();
        TravelPost post = createTravelPost(user);
        UUID postId = post.getId();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        PostDto result = travelPostService.getById(postId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(postId);
        assertThat(result.getContent()).isEqualTo("Amazing place in Tokyo!");
        assertThat(result.getUser().getUsername()).isEqualTo("traveler42");
    }

    @Test
    void getById_throwsNotFoundWhenNotFound() {
        UUID postId = UUID.randomUUID();

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelPostService.getById(postId))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void delete_deletesPostSuccessfully() {
        User user = createUser();
        TravelPost post = createTravelPost(user);
        UUID postId = post.getId();
        UUID userId = user.getId();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        travelPostService.delete(postId, userId);

        verify(postRepository).delete(post);
    }

    @Test
    void delete_throwsForbiddenWhenNotOwner() {
        User user = createUser();
        TravelPost post = createTravelPost(user);
        UUID postId = post.getId();
        UUID differentUserId = UUID.randomUUID();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> travelPostService.delete(postId, differentUserId))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(postRepository, never()).delete(any());
    }

    @Test
    void delete_throwsNotFoundWhenPostNotFound() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelPostService.delete(postId, userId))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(postRepository, never()).delete(any());
    }

    @Test
    void getFeed_delegatesToRepository() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser();
        TravelPost post = createTravelPost(user);
        Page<TravelPost> page = new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository.findFeedForUser(userId, pageable)).thenReturn(page);

        Page<PostDto> result = travelPostService.getFeed(userId, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(postRepository).findFeedForUser(userId, pageable);
    }

    @Test
    void getByDestination_uppercasesCountryCode() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TravelPost> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(postRepository.findByCountryCodeOrderByCreatedAtDesc("JP", pageable)).thenReturn(emptyPage);

        travelPostService.getByDestination("jp", pageable);

        verify(postRepository).findByCountryCodeOrderByCreatedAtDesc("JP", pageable);
    }

    @Test
    void search_delegatesToRepository() {
        String query = "tokyo";
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser();
        TravelPost post = createTravelPost(user);
        Page<TravelPost> page = new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository.searchByContent(query, pageable)).thenReturn(page);

        Page<PostDto> result = travelPostService.search(query, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(postRepository).searchByContent(query, pageable);
    }

    @Test
    void getByUser_delegatesToRepository() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser();
        TravelPost post = createTravelPost(user);
        Page<TravelPost> page = new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(page);

        Page<PostDto> result = travelPostService.getByUser(userId, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(postRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
