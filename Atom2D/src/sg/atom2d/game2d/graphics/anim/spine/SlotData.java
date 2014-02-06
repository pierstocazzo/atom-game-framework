/******************************************************************************
 * Spine Runtimes Software License
 * Version 2
 * 
 * Copyright (c) 2013, Esoteric Software
 * All rights reserved.
 * 
 * You are granted a perpetual, non-exclusive, non-sublicensable and
 * non-transferable license to install, execute and perform the Spine Runtimes
 * Software (the "Software") solely for internal use. Without the written
 * permission of Esoteric Software, you may not (a) modify, translate, adapt or
 * otherwise create derivative works, improvements of the Software or develop
 * new applications using the Software or (b) remove, delete, alter or obscure
 * any trademarks or any copyright, trademark, patent or other intellectual
 * property or proprietary rights notices on or in the Software, including
 * any copy thereof. Redistributions in binary or source form must include
 * this license and terms. THIS SOFTWARE IS PROVIDED BY ESOTERIC SOFTWARE
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL ESOTERIC SOFTARE BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/

package sg.atom2d.game2d.graphics.anim.spine;

import sg.atom2d.game2d.graphics.anim.spine.skeleton.BoneData;
import com.jme3.math.ColorRGBA;

public class SlotData {
	public final String name;
	public final BoneData boneData;
	public final ColorRGBA color = new ColorRGBA(1, 1, 1, 1);
	public String attachmentName;
	public boolean additiveBlending;

	SlotData () {
		name = null;
		boneData = null;
	}

	public SlotData (String name, BoneData boneData) {
		if (name == null) throw new IllegalArgumentException("name cannot be null.");
		if (boneData == null) throw new IllegalArgumentException("boneData cannot be null.");
		this.name = name;
		this.boneData = boneData;
	}

	public String getName () {
		return name;
	}

	public BoneData getBoneData () {
		return boneData;
	}

	public ColorRGBA getColor () {
		return color;
	}

	/** @param attachmentName May be null. */
	public void setAttachmentName (String attachmentName) {
		this.attachmentName = attachmentName;
	}

	/** @return May be null. */
	public String getAttachmentName () {
		return attachmentName;
	}

	public boolean getAdditiveBlending () {
		return additiveBlending;
	}

	public void setAdditiveBlending (boolean additiveBlending) {
		this.additiveBlending = additiveBlending;
	}

	public String toString () {
		return name;
	}
}
