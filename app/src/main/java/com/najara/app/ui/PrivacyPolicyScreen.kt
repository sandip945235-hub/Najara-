package com.najara.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    "Privacy Policy",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            PolicyHeading("Welcome to Najara")
            PolicyText(
                "Najara does not own or host any content. It simply aggregates " +
                "links in a convenient, user-friendly interface. It is an independent " +
                "guide to streaming media available on the web. We do not provide " +
                "streaming content ourselves. This application is designed to enable " +
                "users of Android to easily find and access media content over the " +
                "internet. If you make use of this application you agree to respect " +
                "the rights of the content owners. If not, you should not download " +
                "this application."
            )

            PolicyHeading("Copyright Infringement Policy")
            PolicyText(
                "It is our policy to respond to clear notices of alleged copyright " +
                "infringement. If you are a copyright owner or an agent thereof, and " +
                "you believe that any content in our app infringes your copyrights, " +
                "then you may submit a notification with the following information " +
                "in writing to confirm these requirements:"
            )

            PolicyText(
                "A physical or electronic signature of a person authorized to act " +
                "on behalf of the owner of an exclusive right that is allegedly " +
                "infringed. Identification of the copyrighted work claimed to have " +
                "been infringed, or, if multiple copyrighted works in the app are " +
                "covered by a single notification, a representative list of such " +
                "works in this app."
            )

            PolicyText(
                "Identification of the material that is claimed to be infringing or " +
                "to be the subject of infringing activity and that is to be removed " +
                "or access to which is to be disabled, and information reasonably " +
                "sufficient to permit this app to locate the material. Providing a " +
                "broadcaster's feed and the time on such feed at which you believe " +
                "there has been an infringement is the best way to help us locate " +
                "content quickly."
            )

            PolicyText(
                "Information reasonably sufficient to permit us to contact you, " +
                "such as an address, telephone number, and an electronic mail " +
                "address at which you may be contacted. A statement that you have " +
                "a good faith belief that use of the material in the manner " +
                "complained of is not authorized by the copyright owner, its agent, " +
                "or the law."
            )

            PolicyText(
                "A statement that the information in the notification is accurate, " +
                "and under penalty of perjury, that you are authorized to act on " +
                "behalf of the owner of an exclusive right that is allegedly " +
                "infringed. Please note that any person who knowingly materially " +
                "misrepresents that material or activity is infringing may be " +
                "subject to liability."
            )

            PolicyHeading("Fair Use Notice")
            PolicyText(
                "Before submitting a DMCA notice, it's important to consider if the " +
                "manner in which the material is used falls under fair use. If you " +
                "are not sure whether material located in the app infringes your " +
                "copyright, or if it is subject to fair use protections, you should " +
                "first consider seeking legal advice."
            )

            PolicyHeading("Data Collection")
            PolicyText(
                "Najara does not collect any personal information from users. We do " +
                "not require login or signup. We do not track user activity. No " +
                "personal data is stored, shared, or sold to any third party."
            )

            PolicyHeading("Third-Party Links")
            PolicyText(
                "Our app may contain links to third-party websites or services. We " +
                "are not responsible for the content, privacy policies, or practices " +
                "of any third-party sites or services. We strongly advise you to " +
                "read the privacy policy of every site you visit."
            )

            PolicyHeading("Changes to This Policy")
            PolicyText(
                "We may update our Privacy Policy from time to time. Thus, you are " +
                "advised to review this page periodically for any changes. We will " +
                "notify you of any changes by posting the new Privacy Policy on this " +
                "page. These changes are effective immediately after they are posted."
            )

            PolicyHeading("Contact Us")
            PolicyText(
                "If you have any questions or suggestions about our Privacy Policy, " +
                "do not hesitate to contact us through our Telegram channel or " +
                "email address provided in the app."
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Last Updated: 2025",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PolicyHeading(text: String) {
    Spacer(Modifier.height(16.dp))
    Text(
        text,
        color = Color(0xFFE50914),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun PolicyText(text: String) {
    Text(
        text,
        color = Color(0xFFCCCCCC),
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 20.sp
    )
    Spacer(Modifier.height(8.dp))
}
