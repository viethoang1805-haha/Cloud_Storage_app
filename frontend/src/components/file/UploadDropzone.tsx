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
    const inputRef = useRef<HTMLInputElement>(null)

    return (
        <div
            onClick={() => !isUploading && inputRef.current?.click()}
            className="border-2 border-dashed border-gray-200 rounded-2xl
                 py-24 flex flex-col items-center justify-center
                 hover:border-primary-400 hover:bg-primary-50/50
                 transition-all duration-200 cursor-pointer group"
        >
            <div className="h-14 w-14 bg-gray-100 group-hover:bg-primary-100
                      rounded-2xl flex items-center justify-center mb-4
                      transition-colors">
                {isUploading ? (
                    <Loader2 className="h-7 w-7 text-primary-600 animate-spin" />
                ) : (
                    <Upload className="h-7 w-7 text-gray-400
                             group-hover:text-primary-600 transition-colors" />
                )}
            </div>

            <p className="text-sm font-semibold text-gray-700 mb-1">
                {isUploading ? 'Đang upload...' : 'Kéo & thả file vào đây'}
            </p>
            <p className="text-xs text-gray-400">
                hoặc{' '}
                <span className="text-primary-600 font-medium">
          chọn file
        </span>{' '}
                từ máy tính
            </p>
            <p className="text-xs text-gray-300 mt-2">
                Tối đa 100MB · PDF, Word, Excel, ảnh, video...
            </p>

            <input
                ref={inputRef}
                type="file"
                multiple
                className="hidden"
                onChange={(e) => {
                    if (e.target.files?.length) onFileSelect(e.target.files)
                    e.target.value = ''
                }}
            />
        </div>
    )
}