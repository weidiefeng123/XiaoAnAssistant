const https = require('https');
const fs = require('fs');
const path = require('path');

// 配置 - 请在此填入你的 GitHub Personal Access Token
const GITHUB_TOKEN = process.env.GITHUB_TOKEN || '';
const OWNER = 'weidiefeng123';
const REPO = 'XiaoAnAssistant';

if (!GITHUB_TOKEN) {
    console.error('请设置 GITHUB_TOKEN 环境变量');
    console.error('获取方式：');
    console.error('1. 打开 https://github.com/settings/tokens');
    console.error('2. 点击 "Generate new token (classic)"');
    console.error('3. 勾选 repo 权限');
    console.error('4. 生成后设置环境变量：set GITHUB_TOKEN=你的token');
    process.exit(1);
}

const basePath = 'c:/Users/韦叠凤/CodeBuddy/20260606075517/XiaoAnAssistant';

// 需要上传的文件列表
const filesToUpload = [
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

// gradle-wrapper.jar 需要单独处理（二进制文件）
const binaryFiles = [
    'gradle/wrapper/gradle-wrapper.jar'
];

function githubRequest(method, apiPath, body) {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: 'api.github.com',
            path: apiPath,
            method: method,
            headers: {
                'User-Agent': 'XiaoAnAssistant-Uploader',
                'Authorization': 'token ' + GITHUB_TOKEN,
                'Accept': 'application/vnd.github.v3+json',
                'Content-Type': 'application/json'
            }
        };

        const req = https.request(options, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    const parsed = JSON.parse(data);
                    if (res.statusCode >= 400) {
                        reject(new Error('GitHub API Error ' + res.statusCode + ': ' + (parsed.message || data)));
                    } else {
                        resolve(parsed);
                    }
                } catch (e) {
                    reject(new Error('Parse error: ' + data.substring(0, 200)));
                }
            });
        });

        req.on('error', reject);

        if (body) {
            req.write(JSON.stringify(body));
        }
        req.end();
    });
}

function readFileAsBase64(filePath) {
    const full = path.join(basePath, filePath);
    const content = fs.readFileSync(full);
    return content.toString('base64');
}

async function uploadFiles() {
    console.log('开始上传文件到 GitHub...');

    // 获取仓库信息，检查是否已存在文件
    let existingFiles = {};
    try {
        const ref = await githubRequest('GET', '/repos/' + OWNER + '/' + REPO + '/git/ref/heads/main');
        const tree = await githubRequest('GET', '/repos/' + OWNER + '/' + REPO + '/git/trees/' + ref.object.sha + '?recursive=1');
        if (tree.tree) {
            tree.tree.forEach(item => { existingFiles[item.path] = item.sha; });
        }
    } catch (e) {
        console.log('仓库为空或无法读取，将创建新文件');
    }

    // 准备所有文件
    const allFiles = [...filesToUpload, ...binaryFiles];
    const blobs = [];

    for (let i = 0; i < allFiles.length; i++) {
        const filePath = allFiles[i];
        try {
            const content = readFileAsBase64(filePath);
            console.log('创建 blob: ' + filePath + ' (' + (i + 1) + '/' + allFiles.length + ')');

            const blob = await githubRequest('POST', '/repos/' + OWNER + '/' + REPO + '/git/blobs', {
                content: content,
                encoding: 'base64'
            });

            blobs.push({
                path: filePath,
                mode: filePath.endsWith('.jar') ? '100644' : '100644',
                type: 'blob',
                sha: blob.sha
            });
        } catch (e) {
            console.error('  跳过文件 ' + filePath + ': ' + e.message);
        }
    }

    console.log('已创建 ' + blobs.length + ' 个 blob');

    // 创建 tree
    console.log('创建 tree...');
    const treeResult = await githubRequest('POST', '/repos/' + OWNER + '/' + REPO + '/git/trees', {
        tree: blobs
    });

    // 创建 commit
    console.log('创建 commit...');
    let commitData = {
        message: '初始化小安助手项目 - 智能语音助手App',
        tree: treeResult.sha
    };

    try {
        const ref = await githubRequest('GET', '/repos/' + OWNER + '/' + REPO + '/git/ref/heads/main');
        commitData.parents = [ref.object.sha];
    } catch (e) {
        // 新仓库，没有 parent
    }

    const commit = await githubRequest('POST', '/repos/' + OWNER + '/' + REPO + '/git/commits', commitData);

    // 更新 main 分支引用
    console.log('更新 main 分支...');
    await githubRequest('PATCH', '/repos/' + OWNER + '/' + REPO + '/git/refs/heads/main', {
        sha: commit.sha
    });

    console.log('\n✅ 上传成功！');
    console.log('仓库地址: https://github.com/' + OWNER + '/' + REPO);
    console.log('Actions 页面: https://github.com/' + OWNER + '/' + REPO + '/actions');
}

uploadFiles().catch(err => {
    console.error('上传失败:', err.message);
});
