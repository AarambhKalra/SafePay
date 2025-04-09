const tf = require('@tensorflow/tfjs');
const mobilenet = require('@tensorflow-models/mobilenet');
const axios = require('axios');
const fs = require('fs');
const path = require('path');
const os = require('os');
const ffmpeg = require('fluent-ffmpeg');
const { v4: uuid } = require('uuid');
const { createCanvas, loadImage } = require('canvas');

const FRAME_DIR = path.join(os.tmpdir(), 'frames-' + uuid());
fs.mkdirSync(FRAME_DIR);

// 📥 Download file (video or image) from URL
async function downloadFile(url, ext = 'jpg') {
    const filename = path.join(os.tmpdir(), `${uuid()}.${ext}`);
    const writer = fs.createWriteStream(filename);
    const response = await axios({ url, method: 'GET', responseType: 'stream' });
    response.data.pipe(writer);
    return new Promise((resolve, reject) => {
        writer.on('finish', () => resolve(filename));
        writer.on('error', reject);
    });
}

// 🎞️ Extract 1 frame per second from video
function extractFrames(videoPath, cb) {
    ffmpeg(videoPath)
        .output(path.join(FRAME_DIR, 'frame-%03d.jpg'))
        .outputOptions('-vf', 'fps=1')
        .on('end', () => {
            console.log('✅ Frames extracted.');
            cb();
        })
        .on('error', err => {
            console.error('❌ FFmpeg error:', err);
        })
        .run();
}

// 🧠 Extract feature embeddings
async function extractFeatures(imagePath, model) {
    const img = await loadImage(imagePath);
    const canvas = createCanvas(img.width, img.height);
    const ctx = canvas.getContext('2d');
    ctx.drawImage(img, 0, 0, img.width, img.height);

    const imageTensor = tf.browser.fromPixels(canvas);
    const emb = model.infer(imageTensor, true);
    return emb;
}

// 📊 Cosine similarity
function cosineSimilarity(emb1, emb2) {
    const sim = tf.losses.cosineDistance(emb1, emb2, 0);
    return 1 - sim.dataSync()[0];
}

// 🚀 MAIN FUNCTION
async function verify(videoUrl, imageUrls, threshold = 0.85) {
    console.log('🔍 Starting verification...');
    const model = await mobilenet.load();
    const referenceEmbeddings = [];

    // 📥 Download and embed reference images
    for (const imageUrl of imageUrls) {
        const imagePath = await downloadFile(imageUrl, 'jpg');
        const emb = await extractFeatures(imagePath, model);
        referenceEmbeddings.push({ path: imageUrl, emb });
    }

    // 📥 Download video
    const videoPath = await downloadFile(videoUrl, 'mp4');

    // 🎞️ Extract frames
    extractFrames(videoPath, async () => {
        const frames = fs.readdirSync(FRAME_DIR).filter(f => f.endsWith('.jpg'));
        let bestScore = 0;
        let bestMatch = null;

        for (const frame of frames) {
            const framePath = path.join(FRAME_DIR, frame);
            const frameEmb = await extractFeatures(framePath, model);

            for (const ref of referenceEmbeddings) {
                const sim = cosineSimilarity(frameEmb, ref.emb);
                console.log(`📸 Frame ${frame} vs ${path.basename(ref.path)} → Similarity: ${(sim * 100).toFixed(2)}%`);

                if (sim > bestScore) {
                    bestScore = sim;
                    bestMatch = ref.path;
                }
            }
        }

        if (bestScore >= threshold) {
            console.log(`✅ MATCH FOUND with ${bestMatch} → ${(bestScore * 100).toFixed(2)}%`);
        } else {
            console.log(`❌ No match found. Best score: ${(bestScore * 100).toFixed(2)}%`);
        }
    });
}

module.exports = async function (videoUrl, imageUrls, threshold = 0.6) {
    console.log('🔍 Starting verification...');
    const model = await mobilenet.load();
    const referenceEmbeddings = [];

    for (const imageUrl of imageUrls) {
        const imagePath = await downloadFile(imageUrl, 'jpg');
        const emb = await extractFeatures(imagePath, model);
        referenceEmbeddings.push({ path: imageUrl, emb });
    }

    const videoPath = await downloadFile(videoUrl, 'mp4');

    return new Promise((resolve, reject) => {
        extractFrames(videoPath, async () => {
            try {
                const frames = fs.readdirSync(FRAME_DIR).filter(f => f.endsWith('.jpg'));
                let bestScore = 0;
                let bestMatch = null;

                for (const frame of frames) {
                    const framePath = path.join(FRAME_DIR, frame);
                    const frameEmb = await extractFeatures(framePath, model);

                    for (const ref of referenceEmbeddings) {
                        const sim = cosineSimilarity(frameEmb, ref.emb);
                        console.log(`📸 Frame ${frame} vs ${path.basename(ref.path)} → Similarity: ${(sim * 100).toFixed(2)}%`);

                        if (sim > bestScore) {
                            bestScore = sim;
                            bestMatch = ref.path;
                        }
                    }
                }

                resolve({
                    match: bestScore >= threshold,
                    bestScore: (bestScore * 100).toFixed(2),
                    bestMatch
                });
            } catch (err) {
                reject(err);
            }
        });
    });
};

