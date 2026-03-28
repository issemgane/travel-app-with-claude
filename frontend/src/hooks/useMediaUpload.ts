import { useState, useCallback } from 'react';
import { api } from '@/lib/api';

interface UploadResult {
  mediaUrl: string;
  mediaType: 'IMAGE' | 'VIDEO';
  width?: number;
  height?: number;
}

export function useMediaUpload() {
  const [isUploading, setIsUploading] = useState(false);
  const [progress, setProgress] = useState(0);

  const upload = useCallback(async (file: File): Promise<UploadResult> => {
    setIsUploading(true);
    setProgress(0);

    try {
      // Get presigned URL
      const { uploadUrl, mediaUrl } = await api.getPresignedUrl(file.type, file.name);
      setProgress(20);

      // Upload to S3/MinIO
      await fetch(uploadUrl, {
        method: 'PUT',
        body: file,
        headers: { 'Content-Type': file.type },
      });
      setProgress(100);

      const mediaType = file.type.startsWith('video/') ? 'VIDEO' as const : 'IMAGE' as const;

      return { mediaUrl, mediaType };
    } finally {
      setIsUploading(false);
    }
  }, []);

  return { upload, isUploading, progress };
}
