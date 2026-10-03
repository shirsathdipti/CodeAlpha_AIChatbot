import java.util.*;

public class AIChatbot {

    // ---------- NLP helpers (from Step 2) ----------
    static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "am", "to", "of", "in", "on", "it",
            "do", "does", "can", "you", "i", "me", "my", "please", "tell",
            "about", "what", "your", "for", "u"));

    static String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    static String stem(String word) {
        if (word.endsWith("ing") && word.length() > 5) return word.substring(0, word.length() - 3);
        if (word.endsWith("ed") && word.length() > 4) return word.substring(0, word.length() - 2);
        if (word.endsWith("s") && !word.endsWith("ss") && word.length() > 3) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    static Set<String> tokenize(String text) {
        Set<String> tokens = new LinkedHashSet<>();
        for (String word : normalize(text).split(" ")) {
            if (word.isEmpty() || STOP_WORDS.contains(word)) continue;
            tokens.add(stem(word));
        }
        return tokens;
    }

    static boolean hasPhrase(String normalized, String phrase) {
        return (" " + normalized + " ").contains(" " + phrase + " ");
    }

    // ---------- One topic the bot knows about ----------
    static class Intent {
        final String name;
        final Set<String> keywords = new HashSet<>();
        final String[] phrases;
        final String[] responses;

        Intent(String name, String[] keywords, String[] phrases, String... responses) {
            this.name = name;
            for (String k : keywords) this.keywords.add(stem(k));   // stem them the same way as user words
            this.phrases = phrases;
            this.responses = responses;
        }

        // each matching word = 1 point, each matching phrase = 2 points
        int score(Set<String> tokens, String normalized) {
            int score = 0;
            for (String t : tokens) {
                if (keywords.contains(t)) score++;
            }
            for (String p : phrases) {
                if (hasPhrase(normalized, p)) score += 2;
            }
            return score;
        }
    }

    static String[] w(String... items) { return items; }   // tiny helper to keep the list tidy

    static final List<Intent> INTENTS = new ArrayList<>();
    static final Random RANDOM = new Random();

    static {
        INTENTS.add(new Intent("greeting",
                w("hello", "hi", "hey", "greetings"),
                w("good morning", "good evening", "good afternoon"),
                "Hello! How can I help you today?", "Hi there! What would you like to talk about?"));

        INTENTS.add(new Intent("how_are_you",
                w("doing", "feeling"),
                w("how are you", "how are you doing"),
                "I'm just code, but I'm running great! How about you?"));

        INTENTS.add(new Intent("identity",
                w("chatbot", "bot"),
                w("your name", "who are you"),
                "I'm JavaBot, a chatbot written in Java for my CodeAlpha internship."));

        INTENTS.add(new Intent("java",
                w("java", "jvm", "jdk", "jre", "bytecode"),
                w(),
                "Java is an object-oriented language that runs on the JVM: write once, run anywhere."));

        INTENTS.add(new Intent("oop",
                w("oop", "object", "oriented", "inheritance", "polymorphism", "encapsulation", "abstraction", "pillar"),
                w("object oriented"),
                "OOP has four pillars: encapsulation, inheritance, polymorphism and abstraction."));

        INTENTS.add(new Intent("python",
                w("python"),
                w(),
                "Python is a beginner-friendly, dynamically typed language. Java is statically typed and usually faster."));

        INTENTS.add(new Intent("ai",
                w("ai", "artificial", "intelligence", "nlp"),
                w("machine learning"),
                "AI means building systems that do tasks needing human-like intelligence. "
                        + "I use simple NLP: tokenizing, stop-word removal, stemming and keyword scoring."));

        INTENTS.add(new Intent("joke",
                w("joke", "funny", "laugh"),
                w(),
                "Why do Java developers wear glasses? Because they don't C#!",
                "There are 10 types of people: those who understand binary and those who don't.",
                "A SQL query walks into a bar, walks up to two tables and asks: 'Can I join you?'"));

        INTENTS.add(new Intent("thanks",
                w("thank", "thanks", "appreciate"),
                w(),
                "You're welcome!", "Happy to help!"));

        INTENTS.add(new Intent("codealpha",
                w("codealpha", "internship", "certificate", "intern"),
                w("what is codealpha"),
                "CodeAlpha offers online internships. Interns complete 2 or 3 tasks in their domain, "
                        + "upload the code to GitHub, post a video on LinkedIn and get a certificate."));

        INTENTS.add(new Intent("github",
                w("github", "git", "repository", "repo", "commit"),
                w(),
                "GitHub hosts Git repositories online. You commit your code locally and push it to share it."));

        // Generic topics go LAST so specific topics win ties
        INTENTS.add(new Intent("capabilities",
                w("help", "assist", "support"),
                w("what can you do"),
                "I can chat, answer questions about Java, OOP, Python and AI, and tell jokes. "
                        + "Type 'nlp: your sentence' to see how I read text."));
    }

    // Returns the topic with the highest score, or null if nothing matched
    static Intent bestIntent(Set<String> tokens, String normalized) {
        Intent best = null;
        int bestScore = 0;
        for (Intent intent : INTENTS) {
            int s = intent.score(tokens, normalized);
            if (s > bestScore) {          // ">" keeps the earlier topic when scores tie
                bestScore = s;
                best = intent;
            }
        }
        return best;
    }

    static String reply(String input) {
        String norm = normalize(input);
        Set<String> tokens = tokenize(input);

        // Debug command for your demo: shows tokens and every topic's score
        if (input.toLowerCase().startsWith("nlp:")) {
            String text = input.substring(4);
            Set<String> t = tokenize(text);
            String n = normalize(text);
            StringBuilder sb = new StringBuilder();
            sb.append("\n  Normalized: ").append(n);
            sb.append("\n  Tokens    : ").append(t);
            sb.append("\n  Scores    :");
            boolean any = false;
            for (Intent intent : INTENTS) {
                int s = intent.score(t, n);
                if (s > 0) { sb.append(" ").append(intent.name).append("=").append(s); any = true; }
            }
            if (!any) sb.append(" (no topic matched)");
            Intent best = bestIntent(t, n);
            sb.append("\n  Chosen    : ").append(best == null ? "none" : best.name);
            return sb.toString();
        }

        if (norm.isEmpty()) {
            return "Say something and I'll respond!";
        }

        Intent best = bestIntent(tokens, norm);
        if (best == null) {
            return "Sorry, I didn't understand that. Type 'help' to see what I can do.";
        }
        return best.responses[RANDOM.nextInt(best.responses.length)];
    }

    static boolean isGoodbye(String input) {
        String norm = normalize(input);
        return norm.equals("bye") || norm.equals("goodbye")
                || norm.equals("exit") || norm.equals("quit");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("JavaBot: Hi! I'm JavaBot. Type 'bye' to quit.");

        while (true) {
            System.out.print("You: ");
            String input = sc.nextLine();

            if (isGoodbye(input)) {
                System.out.println("JavaBot: Goodbye!");
                break;
            }
            System.out.println("JavaBot: " + reply(input));
        }
    }
}