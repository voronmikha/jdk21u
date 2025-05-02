package ru.npsystems.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * It is a hybrid hash function that combines elements of popular algorithms
 * with some optimisations for performance in Java.
 */

public class QuantumResistantHash {

    private static final int HASH_ALGORITHM = 2; // 2 = SHA-512
    private static final int TREE_HEIGHT = 10; //


    private static final int N = 32;
    private static final int W = 16;

    /**
     * Constructor
     */
    public QuantumResistantHash() {
    }

    public static byte[] hash(byte[] message) {
        try {
            /**
             * Generation of a random initial value
             */
            SecureRandom random = SecureRandom.getInstanceStrong();
            byte[] seed = new byte[N];
            random.nextBytes(seed);

            /**
             * WOTS+ key pair generation
             */
            byte[][] secretKey = generateWOTSPlusSecretKey(seed);
            byte[][] publicKey = generateWOTSPlusPublicKey(secretKey);

            byte[] root = buildMerkleTree(publicKey);

            MessageDigest digest = MessageDigest.getInstance("SHA-" + (HASH_ALGORITHM * 256));
            digest.update(message);
            digest.update(root);

            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("The algorithm is not supported", e);
        }
    }

    /**
     * WOTS+ key pair generation
     *
     * @param seed Result of generating a random initial value
     * @return
     */
    private static byte[][] generateWOTSPlusSecretKey(byte[] seed) {
        byte[][] secretKey = new byte[N][N];
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-" + (HASH_ALGORITHM * 256));

            for (int i = 0; i < N; i++) {
                md.update(seed);
                md.update((byte) i);
                secretKey[i] = md.digest();
            }

            return secretKey;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Key generation error", e);
        }
    }

    /**
     * @param secretKey Secret key
     * @return publicKey
     */
    private static byte[][] generateWOTSPlusPublicKey(byte[][] secretKey) {
        byte[][] publicKey = new byte[N][N];

        for (int i = 0; i < N; i++) {
            byte[] current = Arrays.copyOf(secretKey[i], N);

            for (int j = 0; j < W; j++) {
                current = hashFunction(current);
            }

            publicKey[i] = current;
        }

        return publicKey;
    }

    /**
     * Merkle tree construction
     *
     * @param leaves
     * @return tree
     */
    private static byte[] buildMerkleTree(byte[][] leaves) {
        int levels = TREE_HEIGHT;
        byte[][] tree = Arrays.copyOf(leaves, leaves.length);

        for (int level = 0; level < levels; level++) {
            int len = tree.length / 2 + (tree.length % 2);
            byte[][] nextLevel = new byte[len][N];

            for (int i = 0; i < len; i++) {
                int left = 2 * i;
                int right = 2 * i + 1;

                if (right < tree.length) {
                    nextLevel[i] = hashFunction(concat(tree[left], tree[right]));
                } else {
                    nextLevel[i] = tree[left];
                }
            }

            tree = nextLevel;
        }

        return tree[0];
    }

    /**
     * Hashing a message with the root of the tree
     *
     * @param input
     * @return digest
     */
    private static byte[] hashFunction(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-" + (HASH_ALGORITHM * 256));
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing error", e);
        }
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static String hashToString(byte[] hash) {
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String hash(String message) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        return hashToString(hash(bytes));
    }
}