import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { useUploadFile } from '@/hooks/useFile'
import UploadDropzone from '@/components/file/UploadDropzone'
import toast from 'react-hot-toast'

export default function UploadPage() {
    const { workspaceId } = useParams<{ workspaceId: string }>()
    const navigate = useNavigate()
    const uploadMutation = useUploadFile(workspaceId!)

    const handleFiles = async (files: FileList) => {
        let success = 0
        for (const file of Array.from(files)) {
            try {
                await uploadMutation.mutateAsync({ file })
                success++
            } catch {}
        }
        if (success > 0) {
            toast.success(`Upload ${success} file thành công!`)
            navigate(`/workspaces/${workspaceId}/files`)
        }
    }

    return (
        <div className="max-w-2xl">
            <div className="flex items-center gap-3 mb-6">
                <button
                    onClick={() => navigate(-1)}
                    className="h-9 w-9 flex items-center justify-center
                     rounded-xl border border-gray-200 text-gray-500
                     hover:bg-gray-50 transition-colors"
                >
                    <ArrowLeft className="h-4 w-4" />
                </button>
                <h1 className="text-xl font-bold text-gray-900">Upload file</h1>
            </div>

            <UploadDropzone
                onFileSelect={handleFiles}
                isUploading={uploadMutation.isPending}
            />
        </div>
    )
}