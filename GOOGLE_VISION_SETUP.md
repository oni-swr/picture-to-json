# Google Cloud Vision API Setup Guide

This guide explains how to set up handwriting recognition using Google Cloud Vision API.

## Prerequisites

1. Google Cloud Platform account
2. Billing enabled (free tier available)
3. Cloud Vision API enabled

## Setup Steps

### 1. Create Google Cloud Project

```bash
# Using gcloud CLI
gcloud projects create your-project-id
gcloud config set project your-project-id
```

### 2. Enable Vision API

```bash
gcloud services enable vision.googleapis.com
```

### 3. Create Service Account

```bash
gcloud iam service-accounts create picture-to-json \
    --display-name="Picture to JSON Service Account"
```

### 4. Grant Permissions

```bash
gcloud projects add-iam-policy-binding your-project-id \
    --member="serviceAccount:picture-to-json@your-project-id.iam.gserviceaccount.com" \
    --role="roles/ml.developer"
```

### 5. Download Credentials

```bash
gcloud iam service-accounts keys create ./google-vision-credentials.json \
    --iam-account=picture-to-json@your-project-id.iam.gserviceaccount.com
```

## Configuration

### Environment Variables

```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/google-vision-credentials.json"
export GOOGLE_VISION_ENABLED=true
export HANDWRITING_AUTO_DETECT=true
export DEFAULT_OCR_ENGINE=TESSERACT
```

### Docker Environment

```yaml
# docker-compose.yml
version: '3.8'
services:
  app:
    build: .
    environment:
      - GOOGLE_APPLICATION_CREDENTIALS=/app/config/google-vision-credentials.json
      - GOOGLE_VISION_ENABLED=true
      - HANDWRITING_AUTO_DETECT=true
      - DEFAULT_OCR_ENGINE=TESSERACT
    volumes:
      - ./google-vision-credentials.json:/app/config/google-vision-credentials.json:ro
```

### Application Configuration

```yaml
# application.yml
app:
  ocr:
    tesseract:
      data-path: /usr/share/tesseract-ocr/4.00/tessdata
      language: eng
    google-vision:
      enabled: ${GOOGLE_VISION_ENABLED:false}
      credentials-path: ${GOOGLE_APPLICATION_CREDENTIALS:}
    handwriting:
      auto-detect: ${HANDWRITING_AUTO_DETECT:true}
    default-engine: ${DEFAULT_OCR_ENGINE:TESSERACT}
```

## Testing

### 1. Check API Availability

```bash
curl "http://localhost:8080/api/documents/ocr/handwriting/available"
# Should return: true
```

### 2. Check Available Engines

```bash
curl "http://localhost:8080/api/documents/ocr/engines"
# Should return: ["TESSERACT", "GOOGLE_VISION"]
```

### 3. Test with Handwritten Document

```bash
# Upload a handwritten document
curl -X POST "http://localhost:8080/api/documents/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@handwritten_form.jpg"

# Process the document (will auto-detect handwriting)
curl -X POST "http://localhost:8080/api/documents/1/process"

# Check results
curl "http://localhost:8080/api/documents/1"
```

## Pricing

Google Vision API pricing (as of 2024):
- Free tier: 1,000 requests per month
- Beyond free tier: $1.50 per 1,000 requests
- Document text detection: Same pricing

## Security Considerations

1. **Credentials Storage**: Store credentials securely outside the application directory
2. **Access Control**: Use IAM roles with minimal required permissions
3. **Data Privacy**: Google Vision API processes images in Google Cloud
4. **Network Security**: Use HTTPS for all API communications

## Troubleshooting

### Common Issues

1. **"Google Vision OCR Engine is not available"**
   - Check GOOGLE_APPLICATION_CREDENTIALS environment variable
   - Verify credentials file exists and is readable
   - Ensure Vision API is enabled in your project

2. **"Permission denied"**
   - Verify service account has ml.developer role
   - Check billing is enabled for your project

3. **"Quota exceeded"**
   - Check your API usage in Google Cloud Console
   - Consider upgrading from free tier if needed

### Debug Mode

Enable debug logging:

```yaml
logging:
  level:
    com.picturetojson.service.ocr: DEBUG
    com.google.cloud.vision: DEBUG
```

## Performance Optimization

1. **Image Preprocessing**: Optimize images before sending to API
2. **Batch Processing**: Process multiple documents efficiently
3. **Caching**: Consider caching results for identical documents
4. **Fallback Strategy**: Always have Tesseract as fallback for printed text

## Alternative Solutions

If Google Vision API is not suitable:

1. **AWS Textract**: Similar functionality with different pricing
2. **Azure Computer Vision**: Microsoft's OCR solution
3. **Open Source**: TensorFlow models for handwriting recognition
4. **Local Models**: Deploy CRNN models locally

For production use, evaluate cost, accuracy, and privacy requirements.