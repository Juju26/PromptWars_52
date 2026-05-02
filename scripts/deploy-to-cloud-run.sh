#!/bin/bash
set -e

# ==========================================
# HackCoord Quick Deployment Script
# Target: Google Cloud Run
# Execution Time: ~15 minutes (mostly waiting for DB)
# ==========================================

PROJECT_ID="handy-bay-495105-a9"
REGION="us-central1"
DB_INSTANCE="hackcoord-db-fast"
DB_PASS="HackCoord123!"
REPO_NAME="hackcoord-repo"

echo "=========================================="
echo "Starting HackCoord Deployment..."
echo "Project: $PROJECT_ID | Region: $REGION"
echo "=========================================="

echo "Configuring gcloud project..."
gcloud config set project $PROJECT_ID

echo "=== 1. Enabling Google Cloud APIs ==="
gcloud services enable \
    run.googleapis.com \
    cloudbuild.googleapis.com \
    artifactregistry.googleapis.com \
    sqladmin.googleapis.com \
    pubsub.googleapis.com

echo "=== 2. Creating Artifact Registry ==="
if ! gcloud artifacts repositories describe $REPO_NAME --location=$REGION > /dev/null 2>&1; then
    gcloud artifacts repositories create $REPO_NAME \
        --repository-format=docker \
        --location=$REGION \
        --description="HackCoord Docker repository"
else
    echo "Artifact Registry already exists."
fi

echo "=== 3. Provisioning Database (Cloud SQL) ==="
echo "Note: This step usually takes 5-8 minutes. Please be patient."
if ! gcloud sql instances describe $DB_INSTANCE --region=$REGION > /dev/null 2>&1; then
    gcloud sql instances create $DB_INSTANCE \
        --database-version=POSTGRES_15 \
        --cpu=1 --memory=4GB \
        --region=$REGION \
        --root-password="$DB_PASS" \
        --assign-ip
else
    echo "Cloud SQL instance already exists."
fi

DB_IP=$(gcloud sql instances describe $DB_INSTANCE --format="value(ipAddresses.ipAddress)")
echo "Database Public IP: $DB_IP"

echo "Creating Application Databases..."
for db in promptwars_users promptwars_tasks promptwars_messaging promptwars_beacon; do
    gcloud sql databases create $db --instance=$DB_INSTANCE || echo "Database $db already exists"
done

echo "=== 4. Setting up Pub/Sub ==="
for topic in hackathon-notifications hackathon-announcements; do
    gcloud pubsub topics create $topic || echo "Topic $topic already exists"
done
gcloud pubsub subscriptions create hackathon-announcements-sub \
    --topic=hackathon-announcements || echo "Subscription already exists"


echo "=== 5. Deploying Backend Services ==="
JWT_SECRET=$(openssl rand -hex 32)
cd BackEnd

# Map service to its database name
declare -A DB_MAP=(
    ["user-service"]="promptwars_users"
    ["task-service"]="promptwars_tasks"
    ["messaging-service"]="promptwars_messaging"
    ["beacon-service"]="promptwars_beacon"
)

# Loop and deploy each service
for SVC in user-service task-service messaging-service beacon-service; do
    DB_NAME=${DB_MAP[$SVC]}
    IMAGE="$REGION-docker.pkg.dev/$PROJECT_ID/$REPO_NAME/$SVC"
    
    echo "----------------------------------------"
    echo "Building $SVC..."
    gcloud builds submit --tag $IMAGE -f $SVC/Dockerfile .
    
    echo "Deploying $SVC to Cloud Run..."
    URL=$(gcloud run deploy $SVC \
        --image $IMAGE \
        --region $REGION \
        --allow-unauthenticated \
        --set-env-vars="DB_URL=jdbc:postgresql://$DB_IP:5432/$DB_NAME,DB_USER=postgres,DB_PASS=$DB_PASS,JWT_SECRET=$JWT_SECRET,GCP_PROJECT_ID=$PROJECT_ID,SPRING_PROFILES_ACTIVE=prod" \
        --format="value(status.url)")
        
    echo "$SVC deployed at: $URL"
    
    # Store the URL to pass to the frontend
    # Convert 'user-service' to 'USER_SERVICE_URL'
    VAR_NAME=$(echo "$SVC" | tr '[:lower:]' '[:upper:]' | sed 's/-/_/')_URL
    export "$VAR_NAME"="$URL"
done

cd ..

echo "=== 6. Deploying Frontend ==="
echo "----------------------------------------"
cd FE
IMAGE="$REGION-docker.pkg.dev/$PROJECT_ID/$REPO_NAME/frontend"

echo "Building Frontend..."
gcloud builds submit --tag $IMAGE -f Dockerfile .

echo "Deploying Frontend to Cloud Run..."
FE_URL=$(gcloud run deploy hackcoord-frontend \
    --image $IMAGE \
    --region $REGION \
    --allow-unauthenticated \
    --set-env-vars="USER_SERVICE_URL=$USER_SERVICE_URL,TASK_SERVICE_URL=$TASK_SERVICE_URL,MESSAGING_SERVICE_URL=$MESSAGING_SERVICE_URL,BEACON_SERVICE_URL=$BEACON_SERVICE_URL" \
    --format="value(status.url)")

echo ""
echo "================================================="
echo "✅ DEPLOYMENT COMPLETE! ✅"
echo "Your HackCoord application is fully live."
echo "Frontend URL: $FE_URL"
echo "================================================="
