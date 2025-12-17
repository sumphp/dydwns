package com.example.own

import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.own.ui.theme.OwnTheme

// --- 1. 데이터 클래스 ---
data class Post(
    val id: String = "",
    val userNickname: String = "",
    val userProfileImageUrl: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val likes: Int = 0
)

// --- 2. MainActivity ---
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OwnTheme {
                var currentScreen by remember { mutableStateOf("home") }

                Scaffold(
                    topBar = { TopAppBar(title = { Text("CLOTH-UP") }) },
                    bottomBar = {
                        BottomAppBar(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { currentScreen = "home" }) {
                                    Icon(Icons.Filled.Home, "Home", tint = if (currentScreen == "home") Color.Black else Color.Gray)
                                }
                                IconButton(onClick = { currentScreen = "search" }) {
                                    Icon(Icons.Default.Search, "Search", tint = if (currentScreen == "search") Color.Black else Color.Gray)
                                }
                                IconButton(onClick = { currentScreen = "upload" }) {
                                    Icon(Icons.Filled.Add, "Upload", tint = if (currentScreen == "upload") Color.Black else Color.Gray)
                                }
                                IconButton(onClick = { currentScreen = "profile" }) {
                                    Icon(Icons.Filled.Person, "Profile", tint = if (currentScreen == "profile") Color.Black else Color.Gray)
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "home" -> FeedScreen()
                            "search" -> SearchScreen()
                            "upload" -> UploadScreen()
                            "profile" -> ProfileScreen()
                        }
                    }
                }
            }
        }
    }
}

// --- 3. SNS 피드 화면  ---
@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    val posts = remember { mutableStateListOf<Post>() }

    val firestore = FirebaseFirestore.getInstance()

    DisposableEffect(Unit) {
        val listener = firestore.collection("posts")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    posts.clear()
                    for (document in snapshot) {
                        val post = document.toObject(Post::class.java)
                        posts.add(post)
                    }
                }
            }
        onDispose {
            listener.remove()
        }
    }

    // 3. 화면 그리기
    if (posts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("로딩 중이거나 게시물이 없습니다...", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(posts) { post ->
                CodyPostCard(post = post)
            }
        }
    }
}

@Composable
fun CodyPostCard(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = post.userProfileImageUrl.ifEmpty { R.drawable.profile_placeholder },
                    contentDescription = "프로필",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.profile_placeholder),
                    error = painterResource(id = R.drawable.profile_placeholder)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = post.userNickname, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            AsyncImage(
                model = post.imageUrl,
                contentDescription = "코디 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = post.description, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "좋아요 ${post.likes}개", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

// --- 5. 사진 업로드 화면 ---
@Composable
fun UploadScreen() {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var description by remember { mutableStateOf("") }

    // 로딩 중인지 확인하는 상태 (업로드 중엔 버튼 못 누르게)
    var isUploading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val storage = FirebaseStorage.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> selectedImageUri = uri }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("NEW CLOTH", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        AsyncImage(
            model = selectedImageUri,
            contentDescription = "선택된 코디 사진",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.LightGray)
                .clickable {
                    if (!isUploading) galleryLauncher.launch("image/*")
                },
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.profile_placeholder)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("코디 설명...") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            enabled = !isUploading
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- 업로드 버튼 ---
        Button(
            onClick = {
                if (selectedImageUri == null) return@Button
                isUploading = true

                val fileName = "post_${UUID.randomUUID()}.jpg"

                val storageRef = storage.reference.child("images/$fileName")

                storageRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()

                            val newPost = Post(
                                id = UUID.randomUUID().toString(),
                                userNickname = "sumphp",
                                userProfileImageUrl = "",
                                imageUrl = imageUrl,
                                description = description,
                                likes = 0
                            )

                            firestore.collection("posts")
                                .add(newPost)
                                .addOnSuccessListener {
                                    isUploading = false
                                    Toast.makeText(context, "업로드 완료!", Toast.LENGTH_SHORT).show()
                                    description = ""
                                    selectedImageUri = null
                                }
                                .addOnFailureListener {
                                    isUploading = false
                                    Toast.makeText(context, "글 저장 실패..", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        isUploading = false
                        Toast.makeText(context, "사진 업로드 실패..", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedImageUri != null && !isUploading
        ) {
            if (isUploading) {
                Text("업로드 중...")
            } else {
                Text("CLOTH-UP! (업로드)")
            }
        }
    }
}

// --- 6. 프로필 화면 ---
@Composable
fun ProfileScreen() {
    val myPosts = remember { mutableStateListOf<Post>() }

    val firestore = FirebaseFirestore.getInstance()

    DisposableEffect(Unit) {
        val listener = firestore.collection("posts")
            // 내 게시물만 가져오기
            .whereEqualTo("userNickname", "sumphp")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    myPosts.clear()
                    for (document in snapshot) {
                        myPosts.add(document.toObject(Post::class.java))
                    }
                }
            }

        onDispose { listener.remove() }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // 프로필 헤더
        NewProfileHeader()

        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)

        // 3. 내 게시물 그리드
        if (myPosts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("아직 올린 코디가 없어요!", color = Color.Gray)
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(myPosts) { post ->
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "내 코디",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0F0F0)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

// --- 7. 프로필 헤더 ---
@Composable
fun NewProfileHeader() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = painterResource(id = R.drawable.profile_placeholder),
                contentDescription = "프로필 사진",
                modifier = Modifier.size(100.dp).clip(CircleShape).border(2.dp, Color.Black, CircleShape),
                contentScale = ContentScale.Crop
            )
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "설정",
                modifier = Modifier.size(28.dp).clip(CircleShape).background(Color.White)
                    .border(1.dp, Color.LightGray, CircleShape).padding(4.dp).clickable { },
                tint = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "sumphp", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text(text = "댄디 혐오하는 사람의 옷장", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            StyleTag(text = "#미니멀")
            Spacer(modifier = Modifier.width(6.dp))
            StyleTag(text = "#스트릿")
            Spacer(modifier = Modifier.width(6.dp))
            StyleTag(text = "#OOTD")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Followers ", color = Color.Gray, fontSize = 12.sp)
            Text(text = "10", fontWeight = FontWeight.Bold, fontSize = 14.sp) // 실제 데이터 연결 전
            Spacer(modifier = Modifier.width(16.dp))
            Divider(modifier = Modifier.height(12.dp).width(1.dp), color = Color.LightGray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Following ", color = Color.Gray, fontSize = 12.sp)
            Text(text = "1022", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun StyleTag(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)).padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

// --- 8. 검색 화면 ---
@Composable
fun SearchScreen() {
    // 검색어 상태
    var searchQuery by remember { mutableStateOf("") }

    // 모든 게시물을 담을 리스트 (원본 데이터)
    val allPosts = remember { mutableStateListOf<Post>() }

    val firestore = FirebaseFirestore.getInstance()

    // 화면 켜지면 일단 모든 게시물 가져오기
    LaunchedEffect(Unit) {
        firestore.collection("posts").get()
            .addOnSuccessListener { result ->
                allPosts.clear()
                for (document in result) {
                    allPosts.add(document.toObject(Post::class.java))
                }
            }
    }

    // 4. 검색 로직
    // 검색어가 없으면 -> 모든 글
    // 검색어가 있으면 -> 설명에 그 단어가 포함된 것만
    val filteredPosts = if (searchQuery.isEmpty()) {
        allPosts
    } else {
        allPosts.filter { post ->
            post.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 검색창
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = {  }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 검색어가 없을 때만 '추천 태그' 띄우기
        if (searchQuery.isEmpty()) {
            Text(
                text = "🔥 지금 뜨는 스타일",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            val trendTags = listOf("#고프코어", "#Y2K", "#힙합", "#시티보이", "#레이어드", "#비니", "#블록코어")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(trendTags) { tag ->
                    // 태그 누르면 검색창에 입력되게
                    StyleTag(text = tag, onClick = { searchQuery = tag.replace("#", "") })
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "취향 저격 코디 발견",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        } else {
            // 검색 중일 때는 결과 개수 건
            Text(
                text = "'$searchQuery' 검색 결과 (${filteredPosts.size}건)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 결과 그리드
        if (filteredPosts.isEmpty() && searchQuery.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("검색 결과가 없어요 😭", color = Color.Gray)
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                modifier = Modifier.weight(1f)
            ) {
                items(filteredPosts) { post ->
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "검색 결과",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0F0F0))
                            .wrapContentHeight(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun StyleTag(text: String, onClick: () -> Unit = {}) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit) {
    TextField(
        value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFF5F5F5)),
        placeholder = { Text(text = "브랜드, 아이템, 스타일 검색", color = Color.Gray, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, "검색", tint = Color.Gray) },
        colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
        singleLine = true
    )
}
