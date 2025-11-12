package com.example.own // 님의 패키지 이름으로 바꾸세요

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.own.ui.theme.OwnTheme // 님의 테마 이름으로 바꾸세요
import androidx.compose.material3.TopAppBar // ⬅️ '3'이 붙은 M3 버전이 필요합니다.
import androidx.compose.material3.ExperimentalMaterial3Api


// --- 1. 가짜 게시물 데이터 클래스 ---
data class Post(
    val id: String,
    val userNickname: String,
    val userProfileImage: Int, // R.drawable.profile_placeholder
    val imageUrl: String, // 웹 URL (Coil이 로드)
    val description: String,
    val likes: Int = 0
)

// --- 2. 가짜 게시물 데이터 목록 ---
// (실제 앱은 Firebase Firestore에서 이 데이터를 가져옵니다)
val dummyPosts = listOf(
    Post(
        id = "1",
        userNickname = "sumphp",
        userProfileImage = R.drawable.profile_placeholder, // ⬅️ res/drawable에 꼭 이미지 추가!
        imageUrl = "https://i.postimg.cc/P5SgpX3T/profile-placeholder.png", // 샘플 이미지
        description = "오늘의 데일리룩! 가을 코디로 딱이죠?",
        likes = 125
    ),
    Post(
        id = "2",
        userNickname = "dydwns",
        userProfileImage = R.drawable.profile_placeholder2, // ⬅️ res/drawable에 꼭 이미지 추가!
        imageUrl = "https://picsum.photos/id/238/600/400",
        description = "새로 산 자켓! 어떤가요? #ootd",
        likes = 88
    )
)

// --- 3. MainActivity ---
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OwnTheme { // 님의 테마 이름으로 바꾸세요

                // ※ 나중에는 Jetpack Navigation으로 FeedScreen과 UploadScreen을 전환하게 됩니다.
                //   지금은 SNS 피드 화면(FeedScreen)을 기본으로 보여줍니다.
                //   UploadScreen을 먼저 보려면 FeedScreen(..) 대신 UploadScreen(..)을 넣으세요.

                Scaffold(
                    // 상단 앱 바 (TopAppBar)
                    topBar = {
                        TopAppBar(
                            title = { Text("CLOTH-UP") }
                        )
                    },
                    // 하단 앱 바 (BottomAppBar)
                    bottomBar = {
                        BottomAppBar(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { /* 홈 화면 */ }) {
                                    Icon(Icons.Filled.Home, contentDescription = "Home")
                                }
                                IconButton(onClick = { /* 업로드 화면 (추후 Navigation으로 연결) */ }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Upload")
                                }
                                IconButton(onClick = { /* 프로필 화면 */ }) {
                                    Icon(Icons.Filled.Person, contentDescription = "Profile")
                                }
                            }
                        }
                    }
                ) { innerPadding -> // Scaffold가 제공하는 안전한 여백 값
                    // --- Scaffold의 내용물: 실제 게시물 피드 ---
                    FeedScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// --- 4. SNS 피드 화면 ---
@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    // LazyColumn: 스크롤 가능한 게시물 목록
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // 카드 사이 간격
    ) {
        items(dummyPosts) { post ->
            CodyPostCard(post = post)
        }
    }
}

// --- 5. 게시물 카드 1개 UI ---
@Composable
fun CodyPostCard(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 게시물 상세 보기 이동 */ }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // --- 유저 프로필, 닉네임 ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = post.userProfileImage),
                    contentDescription = "프로필",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.userNickname,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 게시물 메인 이미지 (Coil 사용) ---
            AsyncImage(
                model = post.imageUrl, // 웹 URL을 바로 로드
                contentDescription = "코디 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- 게시물 설명, 좋아요 ---
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "좋아요 ${post.likes}개",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}


// --- 6. 사진 업로드 화면 ---
@Composable
fun UploadScreen() {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current

    // 갤러리 런처 준비
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri = uri
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("내 코디 올리기", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        // --- 선택된 이미지 미리보기 (Coil 사용) ---
        AsyncImage(
            model = selectedImageUri,
            contentDescription = "선택된 코디 사진",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.LightGray)
                .clickable { // 이미지 영역을 눌러도 갤러리가 열림
                    galleryLauncher.launch("image/*")
                },
            contentScale = ContentScale.Crop,
            // 사진 선택 전, 기본으로 보여줄 이미지 (R.drawable에 'add_photo' 같은 아이콘 추가 권장)
            placeholder = painterResource(id = R.drawable.profile_placeholder)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 코디 설명 입력칸
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("코디 설명...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.weight(1f)) // 업로드 버튼을 아래로 밀기

        // "업로드" 버튼 (지금은 기능 없음)
        Button(
            onClick = {
                // --- (다음 단계: Firebase 연동) ---
                // 1. 'selectedImageUri'를 Firebase Storage에 업로드
                // 2. 'description'과 이미지 URL을 Firebase Firestore에 저장
            },
            modifier = Modifier.fillMaxWidth(),
            // 사진이 선택되어야만 업로드 버튼이 활성화됨
            enabled = selectedImageUri != null
        ) {
            Text("CLOTH-UP! (업로드)")
        }
    }
}

// --- 7. 미리보기(Preview) 함수들 ---

@Preview(showBackground = true, name = "Feed Screen (피드 화면)")
@Composable
fun FeedScreenPreview() {
    OwnTheme {
        FeedScreen()
    }
}

@Preview(showBackground = true, name = "Upload Screen (업로드 화면)")
@Composable
fun UploadScreenPreview() {
    OwnTheme {
        UploadScreen()
    }
}

@Preview(showBackground = true, name = "Post Card (게시물 1개)")
@Composable
fun CodyPostCardPreview() {
    OwnTheme {
        CodyPostCard(post = dummyPosts[0]) // 가짜 데이터 중 첫 번째 것을 보여줌
    }
}