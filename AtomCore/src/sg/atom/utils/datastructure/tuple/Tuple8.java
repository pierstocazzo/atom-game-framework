/*
 * Copyright (c) 2011-2013 GoPivotal, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sg.atom.utils.datastructure.tuple;

/**
 * A tuple that holds eight values
 *
 * @param <T1> The type of the first value held by this tuple
 * @param <T2> The type of the second value held by this tuple
 * @param <T3> The type of the third value held by this tuple
 * @param <T4> The type of the fourth value held by this tuple
 * @param <T5> The type of the fifth value held by this tuple
 * @param <T6> The type of the sixth value held by this tuple
 * @param <T7> The type of the seventh value held by this tuple
 * @param <T8> The type of the eighth value held by this tuple
 *
 * @author Jon Brisbin
 */
public class Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> extends Tuple7<T1, T2, T3, T4, T5, T6, T7> {

	private static final long serialVersionUID = 3070436338779769189L;

	Tuple8(Object... values) {
		super(values);
	}

	/**
	 * Type-safe way to get the eighth object of this {@link Tuple}.
	 *
	 * @return The eighth object, cast to the correct type.
	 */
	@SuppressWarnings("unchecked")
	public T8 getT8() {
		return (T8) get(7);
	}

}
