import { useRef } from 'react'
import { Upload, Loader2 } from 'lucide-react'

interface UploadDropzoneProps {
    onFileSelect: (files: FileList) => void
    isUploading: boolean
}

export default function UploadDropzone({
                                           onFileSelect,
                                           isUploading,
                                       }: UploadDropzoneProps) {
    const fileInputRef = useRef<HTMLInputElement>(null)

    return (
        <div
            onClick={() => fileInputRef.current?.click()}
            className="border-2 border-dashed border-gray-300 rounded-xl
                 py-20 flex flex-col items-center justify-center
                 hover:border-primary hover:bg-primary/5
                 transition-colors cursor-pointer group"
        >
            {isUploading ? (
                <Loader2 className="h-12 w-12 text-primary animate-spin mb-4" />
            ) : (
                <Upload className="h-12 w-12 text-gray-300
                           group-hover:text-primary transition-colors mb-4" />
            )}

            <p className="text-gray-600 font-medium mb-1">
                {isUploading ? 'Đang upload...' : 'Kéo thả file vào đây'}
            </p>
            <p className="text-gray-400 text-sm">
                hoặc <span className="text-primary font-medium">chọn file</span> từ máy tính
            </p>
            <p className="text-gray-400 text-xs mt-2">
                Tối đa 100MB mỗi file
            </p>

            <input
                ref={fileInputRef}
                type="file"
                multiple
                className="hidden"
                onChange={(e) => {
                    if (e.target.files) onFileSelect(e.target.files)
                    e.target.value = ''
                }}
            />
        </div>
    )
}