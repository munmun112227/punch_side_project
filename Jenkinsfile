pipeline {
    agent any

    environment {
        // 請將這裡替換成您「原本打卡系統 EC2」的 Public IP
        PROD_EC2_IP = '32.196.135.163'
    }

    stages {
        stage('1. Checkout Code') {
            steps {
                // 從 GitHub 拉取最新的程式碼到 Jenkins 機器裡
                checkout scm
            }
        }
        
        stage('2. Deploy to EC2') {
            steps {
                // 使用 Jenkins 的憑證管理系統，安全地拿出私鑰
                withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                    sh '''
                        echo "開始遠端連線至正式伺服器進行部署..."
                        
                        # 透過 SSH 連線進正式機，並執行更新與重啟指令
                        ssh -i $SSH_KEY -o StrictHostKeyChecking=no $SSH_USER@$PROD_EC2_IP << 'EOF'
                            cd /home/ec2-user/punch_side_project
                            git pull origin main
                            sudo /usr/local/bin/docker-compose build
                            sudo /usr/local/bin/docker-compose up -d
                        EOF
                        
                        echo "部署順利完成！"
                    '''
                }
            }
        }
    }
}
