package analytics;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.cmu.sphinx.alignment.LongTextAligner;
import edu.cmu.sphinx.api.SpeechAligner;
import edu.cmu.sphinx.result.WordResult;
import web.MindReader;

/**
 * For aligning audio to existing transcription and receiving word timestamps.
 * <br>
 * In order to initialize the aligner you need to specify several data files
 * which might be available on the CMUSphinx website. There should be an
 * acoustic model for your language, a dictionary, an optional G2P model to
 * convert word strings to pronunciation. <br>
 * Currently the audio must have specific format (16khz, 16bit, mono), but in
 * the future other formats will be supported. <br>
 * Text should be a clean text in lower case. It should be cleaned from
 * punctuation marks, numbers and other non-speakable things. In the future
 * automatic cleanup will be supported.
 */
public class Aligner {
	private static final String ACOUSTIC_MODEL_PATH = "resource:/edu/cmu/sphinx/models/en-us/en-us";
	private static final String DICTIONARY_PATH = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
	private static final String TEXT = "one zero zero zero one nine oh two " + "one oh zero one eight zero three";

	public static void align() throws Exception {

		URL audioUrl;
		String transcript;

		audioUrl = Aligner.class.getResource("assets" + MindReader.fileSep + "testAudio.wav");
		transcript = TEXT;

		String acousticModelPath = ACOUSTIC_MODEL_PATH;
		String dictionaryPath = DICTIONARY_PATH;
		String g2pPath = null;
		SpeechAligner aligner = new SpeechAligner(acousticModelPath, dictionaryPath, g2pPath);

		List<WordResult> results = aligner.align(audioUrl, transcript);
		List<String> stringResults = new ArrayList<String>();
		for (WordResult wr : results) {
			stringResults.add(wr.getWord().getSpelling());
		}

		LongTextAligner textAligner = new LongTextAligner(stringResults, 2);
		List<String> sentences = aligner.getTokenizer().expand(transcript);
		List<String> words = aligner.sentenceToWords(sentences);

		int[] aid = textAligner.align(words);

		int lastId = -1;
		for (int i = 0; i < aid.length; ++i) {
			if (aid[i] == -1) {
				System.out.format("- %s\n", words.get(i));
			} else {
				if (aid[i] - lastId > 1) {
					for (WordResult result : results.subList(lastId + 1, aid[i])) {
						System.out.format("+ %-25s [%s]\n", result.getWord().getSpelling(), result.getTimeFrame());
					}
				}
				System.out.format("  %-25s [%s]\n", results.get(aid[i]).getWord().getSpelling(),
						results.get(aid[i]).getTimeFrame());
				lastId = aid[i];
			}
		}

		if (lastId >= 0 && results.size() - lastId > 1) {
			for (WordResult result : results.subList(lastId + 1, results.size())) {
				System.out.format("+ %-25s [%s]\n", result.getWord().getSpelling(), result.getTimeFrame());
			}
		}
	}
}