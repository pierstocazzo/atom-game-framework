package sg.atom.assets.json;

import java.io.InputStream;

import sg.atom.core.asset.FileHandle;

public interface BaseJsonReader {
	JsonValue parse (InputStream input);
	JsonValue parse (FileHandle file);
}