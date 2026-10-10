$namespace = 'ecom-order-summary-db-ns'
$secretName = 'ecom-order-summary-cluster-secret'
$securePassword = Read-Host 'Enter the MySQL root password' -AsSecureString
$passwordPointer = [IntPtr]::Zero

try {
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $password = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)

    if ([string]::IsNullOrEmpty($password)) {
        throw 'The MySQL root password must not be empty.'
    }

    $namespaceManifest = [ordered]@{
        apiVersion = 'v1'
        kind = 'Namespace'
        metadata = @{ name = $namespace }
    } | ConvertTo-Json -Depth 5

    $namespaceManifest | kubectl apply -f -
    if ($LASTEXITCODE -ne 0) {
        throw 'Failed to create or update the Kubernetes namespace.'
    }

    $secretManifest = [ordered]@{
        apiVersion = 'v1'
        kind = 'Secret'
        metadata = @{
            name = $secretName
            namespace = $namespace
        }
        type = 'Opaque'
        stringData = @{
            rootUser = 'root'
            rootHost = '%'
            rootPassword = $password
        }
    } | ConvertTo-Json -Depth 5

    $secretManifest | kubectl apply -f -
    if ($LASTEXITCODE -ne 0) {
        throw 'Failed to create or update the InnoDB Cluster Secret.'
    }

    Write-Output "Secret '$secretName' is ready in namespace '$namespace'."
}
finally {
    $password = $null
    $secretManifest = $null
    if ($passwordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
    $securePassword.Dispose()
}
