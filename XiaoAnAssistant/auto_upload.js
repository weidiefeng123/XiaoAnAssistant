const https = require('https');
const fs = require('fs');
const path = require('path');

const OWNER = 'weidiefeng123';
const REPO = 'XiaoAnAssistant';
const BASE = 'c:/Users/韦叠凤/CodeBuddy/20260606075517/XiaoAnAssistant';

function apiCall(method, apiPath, body) {
    return new Promise((resolve, reject) => {
        const opts = {
            hostname: 'api.github.com',
            path: apiPath,
            method: method,
            headers: {
                'User-Agent': 'XiaoAn-Uploader',
                'Accept': 'application/vnd.github.v3+json',
                'Content-Type': 'application/json'
            }
        };
        if (body) opts.headers['Content-Length'] = Buffer.byteLength(JSON.stringify(body));

        const req = https.request(opts, res => {
            let d = '';
            res.on('data', c => d += c);
            res.on('end', () => {
                try {
                    const parsed = JSON.parse(d);
                    if (res.statusCode >= 400) {
                        reject(new Error('API ' + res.statusCode + ': ' + (parsed.message || d).substring(0, 200)));
                    } else {
                        resolve(parsed);
                    }
                } catch (e) { reject(new Error(d.substring(0, 200))); }
            });
        });
        req.on('error', reject);
        if (body) req.write(JSON.stringify(body));
        req.end();
    });
}

function fileToBase64(relPath) {
    return fs.readFileSync(path.join(BASE, relPath)).toString('base64');
}

const textFiles = [
    '.github/workflows/build.yml',
    '.gitignore',
    'README.md',
    'app/build.gradle',
    'app/proguard-rules.pro',
    'app/src/main/AndroidManifest.xml',
    'app/src/main/java/com/xiaoan/assistant/ChatAdapter.java',
    'app/src/main/java/com/xiaoan/assistant/ChatMessage.java',
    'app/src/main/java/com/xiaoan/assistant/LoginActivity.java',
    'app/src/main/java/com/xiaoan/assistant/MainActivity.java',
    'app/src/main/res/drawable/btn_mic_bg.xml',
    'app/src/main/res/drawable/btn_send_bg.xml',
    'app/src/main/res/drawable/circle_bg.xml',
    'app/src/main/res/drawable/ic_launcher_foreground.xml',
    'app/src/main/res/drawable/ic_lock.xml',
    'app/src/main/res/drawable/ic_person.xml',
    'app/src/main/res/drawable/input_bg.xml',
    'app/src/main/res/drawable/msg_assistant_bg.xml',
    'app/src/main/res/drawable/msg_user_bg.xml',
    'app/src/main/res/layout/activity_login.xml',
    'app/src/main/res/layout/activity_main.xml',
    'app/src/main/res/layout/item_msg_assistant.xml',
    'app/src/main/res/layout/item_msg_user.xml',
    'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml',
    'app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml',
    'app/src/main/res/values/colors.xml',
    'app/src/main/res/values/strings.xml',
    'app/src/main/res/values/themes.xml',
    'app/src/main/res/xml/backup_rules.xml',
    'build.gradle',
    'gradle.properties',
    'gradle/wrapper/gradle-wrapper.properties',
    'gradlew',
    'gradlew.bat',
    'settings.gradle',
];

const binaryFiles = [
    'gradle/wrapper/gradle-wrapper.jar'
];

async function main() {
    const allFiles = [...textFiles, ...binaryFiles];
    console.log('Starting upload of ' + allFiles.length + ' files...');

    // Step 1: Create blobs
    const treeEntries = [];
    for (let i = 0; i < allFiles.length; i++) {
        const fp = allFiles[i];
        try {
            const content = fileToBase64(fp);
            process.stdout.write('\rCreating blob ' + (i + 1) + '/' + allFiles.length + ': ' + fp.substring(0, 50) + '        ');
            const blob = await apiCall('POST', '/repos/' + OWNER + '/' + REPO + '/git/blobs', {
                content: content,
                encoding: 'base64'
            });
            treeEntries.push({
                path: fp,
                mode: '100644',
                type: 'blob',
                sha: blob.sha
            });
        } catch (e) {
            console.error('\nSkip ' + fp + ': ' + e.message);
        }
    }

    console.log('\nCreated ' + treeEntries.length + ' blobs');

    // Step 2: Create tree
    console.log('Creating tree...');
    const tree = await apiCall('POST', '/repos/' + OWNER + '/' + REPO + '/git/trees', {
        tree: treeEntries
    });
    console.log('Tree SHA:', tree.sha);

    // Step 3: Create commit (no parent since repo is empty)
    console.log('Creating commit...');
    const commit = await apiCall('POST', '/repos/' + OWNER + '/' + REPO + '/git/commits', {
        message: '初始化小安助手项目 - 智能语音助手App',
        tree: tree.sha
    });
    console.log('Commit SHA:', commit.sha);

    // Step 4: Update main ref
    console.log('Updating main branch...');
    try {
        await apiCall('PATCH', '/repos/' + OWNER + '/' + REPO + '/git/refs/heads/main', {
            sha: commit.sha
        });
    } catch (e) {
        // If ref doesn't exist yet, create it
        console.log('Creating main branch...');
        await apiCall('POST', '/repos/' + OWNER + '/' + REPO + '/git/refs', {
            ref: 'refs/heads/main',
            sha: commit.sha
        });
    }

    console.log('\n========================================');
    console.log('  Upload SUCCESS!');
    console.log('  Repo: https://github.com/' + OWNER + '/' + REPO);
    console.log('  Actions: https://github.com/' + OWNER + '/' + REPO + '/actions');
    console.log('========================================');
}

main().catch(err => {
    console.error('\nFailed:', err.message);
});
