import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

public class BotByteMe implements BotAPI
{

    private static PlayerAPI me;
    private OpponentAPI opponent;
    private BoardAPI board;
    private UserInterfaceAPI info;
    private DictionaryAPI dictionary;
    private int turnCount;

    BotByteMe(PlayerAPI me, OpponentAPI opponent, BoardAPI board, UserInterfaceAPI ui, DictionaryAPI dictionary)
    {
        this.me = me;
        this.opponent = opponent;
        this.board = board;
        this.info = ui;
        this.dictionary = dictionary;
        turnCount = 0;
    }

    public static void main(String[] args) throws FileNotFoundException
    {
        Trie trie = new Trie();
        trie.initTrie();
        ArrayList<Character> array = new ArrayList<>();

        //This adds the current frame to the trie that is searched for a solution
        for (char c : (me.getFrameAsString().toCharArray()))
        {
            array.add(c);
        }

        ArrayList<String> words = trie.constructWords(array);
        System.out.println(words);
        System.out.println(getLongestWord(words));
    }

    public boolean checkAdjacent(int row, int col) // this method checks to see if a square is adjacent to any other tiles on the board
    {
        if(board.getSquareCopy(row, col).equals(' ') && (!board.getSquareCopy(row+1, col).equals(' ') || !board.getSquareCopy(row, col+1).equals(' ')))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    // this method finds all the anchor squares then puts them in an iterable arraylist
    public ArrayList<Square> findAnchors()
    {
        ArrayList<Square> anchors = new ArrayList<>();
        for(int i = 0; i<=15; i++)
        {
            for(int j = 0; j<=15; j++)
            {
                if(checkAdjacent(i, j))
                {
                    anchors.add(board.getSquareCopy(i, j));
                }
            }
        }
        return anchors;
    }

    //this method finds the limit of how much a word can be extended to the left passing a given anchor square, similar method to make one for checking the limit below
    public int findLimitLeft(Square anchor)
    {
        //need to figure out a way to get the x coordinate of the square following his restrictions
        // refer to above comment
        int limit = 0;
        int x = anchor.getX();
        int y = anchor.getY() - 1;

        while (board.getSquareCopy(x,y).equals(' '))
        {
            limit++;
            y = y - 1;
        }

        return limit;
    }

    // returns the first word of longest length
    public static String getLongestWord(ArrayList<String> words)
    {
        String longest = words.get(0);

        for(String word : words)
        {
            if(word.length() > longest.length())
                longest = word;
        }

        return longest;
    }

    public String getCommand()
    {
        String command = "";
        return command;
    }

    public static class Trie
    {
        // initTree() - constructs the tree
        // findWord(String word) - returns true if word is in the trie, false otherwise


        // Nested Trie Node class
        private static class TrieNode
        {


            //  Each node holds a character, a HashTable of children node, and an end of word variable, eow, of type boolean
            //  which indicates if the current node is the end of a word or not

            char letter;
            Hashtable<Character, TrieNode> children;
            // End of Word Node - true if current node is end of a valid word
            boolean eow;

            public TrieNode(char letter)
            {
                this.letter = letter;
                children = new Hashtable<Character, TrieNode>();
            }

            // returns a hashtable of all children nodes of current node
            public Hashtable<Character, TrieNode> getChildren()
            {
                return children;
            }

            public boolean isEow()
            {
                return eow;
            }

            // sets end of word to true
            public void setEow()
            {
                eow = true;
            }

            public char getLetter()
            {
                return letter;
            }

            // add a child not with a given character as letter
            public TrieNode addChild(Character letter)
            {
                // create new node
                // add to HashTable of children of current node
                // return the newly constructed child node

                TrieNode child = new TrieNode(letter);
                children.put(letter, child);
                return child;
            }

        }

        // Root Node of the trie
        private static TrieNode rootNode = new TrieNode(' ');

        //  Initialise the Trie
        //  Calls addWordsToTrie to add all of the words in the dictionary to the tree
        public static void initTrie() throws FileNotFoundException
        {
            addWordsToTrie();
        }


        // Adds each word in the dictionary to the trie
        private static void addWordsToTrie() throws FileNotFoundException
        {

            String inputFileName = "csw.txt";
            File inputFile = new File(inputFileName);
            Scanner dictionary = new Scanner(inputFile);

            while(dictionary.hasNextLine())
            {
                // convert the current word (String) in th dictionary to character array
                // call addWordToTrie passing in the root node, the word character array, and the index 0
                char[] wordarray = dictionary.nextLine().toCharArray();
                addWordToTrie(rootNode, wordarray, 0);
            }
        }

        // recursively add each letter of a word to the trie

        /**
         *
         * @param currentNode, the node in the trie which we are currently on
         * @param word, the word we are adding to the trie
         * @param letterIndex, the index of the current letter in the word
         */
        private static void addWordToTrie(TrieNode currentNode, char[] word, int letterIndex)
        {
            // base case: if we have reached the end of the word
            if (letterIndex >= word.length)
            {
                // set node end of word to true
                currentNode.setEow();
                return;
            }

            // get current letter
            char letter = word[letterIndex];
            TrieNode child;

            // if current letter is already letter of a child node of current node
            if (currentNode.getChildren().containsKey(letter))
            {
                // child = get the child node corresponding to current letter
                child = currentNode.getChildren().get(letter);
            }

            // if current letter is not already letter of a child node of current node
            else
            {
                // child = add a new node with the current letter as character as a child of current node
                child = currentNode.addChild(letter);
            }

            // call again passing in child node, the same word, and the next index
            addWordToTrie(child, word, letterIndex + 1);
        }

        /**
         *
         * @param word, the word we are searching for
         * @return true if the word is in the trie, false if not
         */
        public static boolean findWord(String word)
        {
            // call recursive method to find word and pass in root, word and index 0
            return findWordRecursively(rootNode, word, 0);
        }

        /**
         *
         * @param currentNode, the node in the trie which we are currently on
         * @param word, the word we are searching for
         * @param letterIndex, the index of the current letter in the word
         * @return true if the word is in the trie, false if not
         */
        private static boolean findWordRecursively(TrieNode currentNode, String word, int letterIndex)
        {
            // Base Case: Finished searching through all the letters of the word
            if (letterIndex >= word.length())
            {
//                if (currentNode.isEow()) // if the current node we are on is the end of a word
//                    return true; // this is a valid word - return true
//                else
//                    return false; // not end of word - not a valid word - return false

                return currentNode.isEow();
            }

            // current letter
            char letter = word.charAt(letterIndex);

            // if a node with current letter as character is a child of current node
            if (currentNode.getChildren().containsKey(letter))
            {
                // Continue searching for next letter
                // child node with character = current letter
                TrieNode child = currentNode.getChildren().get(letter);

                // call again for child node, same word, and next index
                return findWordRecursively(child, word, letterIndex + 1);
            }

            // no child of current node with character = current letter
            else
            {
                // word not in trie - return false
                return false;
            }
        }


        // returns an array list of all valid words which can be made from the characters in an input array list of characters
        // (words with no repetition of letters)

        public ArrayList<String> constructWords(ArrayList<Character> letters)
        {
            ArrayList<String> words = new ArrayList<>();
            constructWords(rootNode, letters, "", words);
            return words;
        }

        public void constructWords(TrieNode node, ArrayList<Character> letters, String s, ArrayList<String> words)
        {
            // if we have reached the end of the word (or if the node is the root node - when we first call)
            if(node != rootNode && node.eow)
            {
                // add the word to the ArrayList
                words.add(s);
            }

            // if we have gone through all letters
            if(letters.isEmpty())
                return;

            // for each of the characters in the ArrayList letters
            for(Character ch : letters)
            {
                // if the current node has a child with letter == ch
                if(node.getChildren().containsKey(ch))
                {

                    ArrayList<Character> temp = new ArrayList<>();

                    // add all of the characters in letters to temp
                    for(Character c : letters)
                    {
                        temp.add(c);
                    }

                    // remove ch
                    temp.remove(ch);

                    // temp = letters - ch
                    TrieNode child = node.getChildren().get(ch);
                    constructWords(child, temp, s + ch, words);
                }
            }
        }

    }
}
